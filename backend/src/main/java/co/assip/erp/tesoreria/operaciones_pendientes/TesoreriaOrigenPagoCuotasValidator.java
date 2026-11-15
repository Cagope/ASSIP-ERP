
package co.assip.erp.tesoreria.operaciones_pendientes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TesoreriaOrigenPagoCuotasValidator implements TesoreriaOrigenFinancieroValidator {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /** Identifica exclusivamente el recaudo bancario de Pago de Cuotas V1. */
    @Override
    public boolean soporta(Map<String, Object> operacion) {
        return operacion != null
                && "CARTERA".equals(texto(operacion, "modulo_origen"))
                && "PAGO_CUOTAS".equals(texto(operacion, "proceso_origen"))
                && "cartera.pagos_cuotas".equals(texto(operacion, "tabla_origen"));
    }

    /** Valida el pago original para consulta, sin bloquear ni modificar registros. */
    @Transactional(readOnly = true)
    @Override
    public void validarVistaPrevia(Map<String, Object> operacion) {
        validar(operacion, false);
    }

    /** Valida y bloquea el pago original dentro de la transacción de Tesorería. */
    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void validarConfirmacion(Map<String, Object> operacion) {
        validar(operacion, true);
    }

    /** Recupera la cuenta CxC realmente debitada en el comprobante CC original. */
    @Transactional(readOnly = true)
    @Override
    public Integer obtenerCuentaCxCOriginal(Map<String, Object> operacion) {
        validar(operacion, false);

        Long idPago = enteroLargo(operacion, "id_origen");
        Integer agencia = Math.toIntExact(enteroLargo(operacion, "id_agencia").longValue());
        BigDecimal valorPublicado = importe(operacion, "valor_original");

        List<Map<String, Object>> cuentas = jdbcTemplate.queryForList("""
                SELECT ac.id_catalogo_cuenta,
                       SUM(ac.valor_debito) AS total_debito
                FROM cartera.pagos_cuotas pc
                JOIN contabilidad.origen_comprobantes oc
                  ON oc.id_agencia = pc.id_agencia_receptora
                 AND oc.tipo_comprobante = pc.tipo_comprobante
                 AND oc.numero_comprobante = pc.numero_comprobante
                 AND oc.origen_tipo = 'AUTO'
                 AND oc.estado_origen = 'ACTIVO'
                 AND oc.modulo_origen = 'CARTERA'
                 AND oc.proceso_origen = 'PAGO_CUOTAS'
                 AND oc.tabla_origen = 'cartera.pagos_cuotas'
                 AND oc.id_origen = pc.id_pago
                JOIN contabilidad.auxiliares_contables ac
                  ON ac.id_agencia = oc.id_agencia
                 AND ac.tipo_comprobante = oc.tipo_comprobante
                 AND ac.numero_comprobante = oc.numero_comprobante
                JOIN contabilidad.catalogo_cuentas cc
                  ON cc.id_catalogo_cuenta = ac.id_catalogo_cuenta
                 AND cc.id_agencia = ac.id_agencia
                WHERE pc.id_pago = ?
                  AND pc.id_agencia_receptora = ?
                  AND pc.estado = 'APLICADO'
                  AND ac.estado_movimiento = 'A'
                  AND ac.valor_debito > 0
                  AND ac.valor_credito = 0
                  AND (
                      ac.detalle_movimiento LIKE
                          'RECAUDO POR CONSIGNACIÓN O TRANSFERENCIA - %'
                      OR ac.detalle_movimiento LIKE
                          'RECAUDO CONSIGNACIÓN O TRANSFERENCIA - %'
                  )
                  AND cc.cuenta_operable = TRUE
                GROUP BY ac.id_catalogo_cuenta
                """, idPago, agencia);

        if (cuentas.size() != 1) {
            throw new IllegalStateException(
                    "No se identificó una única cuenta CxC en el "
                            + "comprobante original del pago " + idPago + ".");
        }

        Map<String, Object> cuenta = cuentas.get(0);
        BigDecimal totalDebito = importe(cuenta, "total_debito");

        if (totalDebito.compareTo(valorPublicado) != 0) {
            throw new IllegalStateException(
                    "El débito CxC del comprobante original no coincide "
                            + "con el valor publicado en Tesorería.");
        }

        return Math.toIntExact(enteroLargo(cuenta, "id_catalogo_cuenta"));
    }

    /** Verifica la vigencia, agencia, medios y comprobante del pago original. */
    private void validar(Map<String, Object> operacion, boolean bloquear) {
        if (operacion == null) {
            throw new IllegalArgumentException(
                    "La operación de Tesorería es obligatoria.");
        }

        if (!"CARTERA".equals(texto(operacion, "modulo_origen"))
                || !"PAGO_CUOTAS".equals(texto(operacion, "proceso_origen"))
                || !"cartera.pagos_cuotas".equals(texto(operacion, "tabla_origen"))) {
            throw new IllegalArgumentException(
                    "El validador corresponde únicamente a Pago de Cuotas V1.");
        }

        if (!"ENTRADA".equals(texto(operacion, "naturaleza"))) {
            throw new IllegalStateException(
                    "El recaudo de Pago de Cuotas debe ser una entrada.");
        }

        Long idPago = enteroLargo(operacion, "id_origen");
        Integer agencia = Math.toIntExact(enteroLargo(operacion, "id_agencia"));
        BigDecimal valorPublicado = importe(operacion, "valor_original");

        if (idPago <= 0 || agencia <= 0 || valorPublicado.signum() <= 0) {
            throw new IllegalStateException(
                    "La obligación tiene identificador, agencia o valor inválidos.");
        }

        String sql = """
                SELECT id_pago, id_agencia_receptora, estado, valor_pago,
                       medios_recaudo::text AS medios_recaudo,
                       tipo_comprobante, numero_comprobante
                FROM cartera.pagos_cuotas
                WHERE id_pago = ?
                """ + (bloquear ? " FOR UPDATE" : "");

        List<Map<String, Object>> pagos = jdbcTemplate.queryForList(sql, idPago);

        if (pagos.size() != 1) {
            throw new IllegalStateException(
                    "No existe un único pago de Cartera para la operación de Tesorería.");
        }

        Map<String, Object> pago = pagos.get(0);

        if (!"APLICADO".equals(texto(pago, "estado"))) {
            throw new IllegalStateException(
                    "El pago de Cartera no está vigente; "
                            + "no se permite su recaudo bancario.");
        }

        if (!Objects.equals(
                enteroLargo(pago, "id_agencia_receptora"),
                agencia.longValue())) {
            throw new IllegalStateException(
                    "La agencia del pago no coincide con la obligación de Tesorería.");
        }

        String tipo = texto(pago, "tipo_comprobante");
        String numero = texto(pago, "numero_comprobante");

        if (!"CC".equals(tipo) || numero.isEmpty()) {
            throw new IllegalStateException(
                    "El pago no tiene comprobante CC original válido.");
        }

        BigDecimal totalMedios = BigDecimal.ZERO;
        BigDecimal totalCxC = BigDecimal.ZERO;

        try {
            JsonNode medios = objectMapper.readTree(
                    texto(pago, "medios_recaudo"));

            if (medios == null || !medios.isArray() || medios.isEmpty()) {
                throw new IllegalStateException(
                        "Los medios de recaudo del pago están incompletos.");
            }

            for (JsonNode medio : medios) {
                JsonNode tipoMedio = medio.path("tipoMedio");
                JsonNode valor = medio.path("valor");

                if (!tipoMedio.isTextual() || !valor.isNumber()) {
                    throw new IllegalStateException(
                            "El pago contiene un medio de recaudo inválido.");
                }

                String clase = tipoMedio.asText();

                if (!"CAJA".equals(clase)
                        && !"AHORROS".equals(clase)
                        && !"CXP".equals(clase)) {
                    throw new IllegalStateException(
                            "El pago contiene un tipo de recaudo no reconocido.");
                }

                BigDecimal valorMedio = valor.decimalValue();

                if (valorMedio.signum() <= 0) {
                    throw new IllegalStateException(
                            "El pago contiene un importe de recaudo no positivo.");
                }

                totalMedios = totalMedios.add(valorMedio);

                if ("CXP".equals(clase)) {
                    totalCxC = totalCxC.add(valorMedio);
                }
            }
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "No fue posible interpretar los medios de recaudo del pago.", ex);
        }

        if (totalMedios.compareTo(importe(pago, "valor_pago")) != 0) {
            throw new IllegalStateException(
                    "Los medios de recaudo no coinciden con el valor del pago original.");
        }

        if (totalCxC.signum() <= 0
                || totalCxC.compareTo(valorPublicado) != 0) {
            throw new IllegalStateException(
                    "La CxC del pago no coincide con el valor publicado en Tesorería.");
        }

        Integer comprobantes = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM contabilidad.origen_comprobantes
                WHERE id_agencia = ?
                  AND tipo_comprobante = ?
                  AND numero_comprobante = ?
                  AND origen_tipo = 'AUTO'
                  AND estado_origen = 'ACTIVO'
                  AND modulo_origen = 'CARTERA'
                  AND proceso_origen = 'PAGO_CUOTAS'
                  AND tabla_origen = 'cartera.pagos_cuotas'
                  AND id_origen = ?
                """, Integer.class, agencia, tipo, numero, idPago);

        if (comprobantes == null || comprobantes != 1) {
            throw new IllegalStateException(
                    "El comprobante original no corresponde al pago de Cartera.");
        }
    }

    private static String texto(Map<String, Object> fila, String campo) {
        return Objects.toString(fila.get(campo), "").trim();
    }

    private static Long enteroLargo(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);

        if (!(valor instanceof Number numero)) {
            throw new IllegalStateException(
                    "Falta el identificador obligatorio: " + campo);
        }

        return numero.longValue();
    }

    private static BigDecimal importe(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);

        if (!(valor instanceof Number numero)) {
            throw new IllegalStateException(
                    "Falta el importe obligatorio: " + campo);
        }

        return valor instanceof BigDecimal decimal
                ? decimal
                : new BigDecimal(numero.toString());
    }
}
