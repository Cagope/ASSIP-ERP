
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

/** Valida el recaudo bancario de una prórroga contra su pago y comprobante CC originales. */
@Component
@RequiredArgsConstructor
public class TesoreriaOrigenProrrogasValidator implements TesoreriaOrigenFinancieroValidator {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /** Identifica exclusivamente el recaudo bancario de una prórroga de Cartera. */
    @Override
    public boolean soporta(Map<String, Object> operacion) {
        return operacion != null
                && "CARTERA".equals(texto(operacion, "modulo_origen"))
                && "PRORROGA".equals(texto(operacion, "proceso_origen"))
                && "cartera.creditos_prorrogas".equals(texto(operacion, "tabla_origen"));
    }

    /** Comprueba el origen sin bloquear registros durante la vista previa. */
    @Transactional(readOnly = true)
    @Override
    public void validarVistaPrevia(Map<String, Object> operacion) {
        validar(operacion, false);
    }

    /** Bloquea prórroga y pago en ese orden antes de bloquear la operación de Tesorería. */
    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void validarConfirmacion(Map<String, Object> operacion) {
        validar(operacion, true);
    }

    /** Recupera la cuenta CxC debitada originalmente; nunca usa la parametrización vigente. */
    @Transactional(readOnly = true)
    @Override
    public Integer obtenerCuentaCxCOriginal(Map<String, Object> operacion) {
        Origen origen = validar(operacion, false);
        List<Map<String, Object>> cuentas = jdbcTemplate.queryForList("""
                SELECT ac.id_catalogo_cuenta, SUM(ac.valor_debito) AS total_debito
                FROM contabilidad.auxiliares_contables ac
                JOIN contabilidad.catalogo_cuentas cc
                  ON cc.id_catalogo_cuenta = ac.id_catalogo_cuenta
                 AND cc.id_agencia = ac.id_agencia
                WHERE ac.id_agencia = ?
                  AND ac.tipo_comprobante = 'CC'
                  AND TRIM(ac.numero_comprobante) = ?
                  AND ac.estado_movimiento = 'A'
                  AND ac.valor_debito > 0
                  AND ac.valor_credito = 0
                  AND ac.detalle_movimiento LIKE 'RECAUDO CONSIGNACIÓN O TRANSFERENCIA - %'
                GROUP BY ac.id_catalogo_cuenta
                """, origen.agencia(), origen.numeroComprobante());
        if (cuentas.size() != 1) {
            throw new IllegalStateException("No existe una única cuenta CxC en el CC original de la prórroga.");
        }
        Map<String, Object> cuenta = cuentas.get(0);
        if (importe(cuenta, "total_debito").compareTo(origen.valorCxC()) != 0) {
            throw new IllegalStateException("El débito CxC del CC original no coincide con el valor publicado.");
        }
        return Math.toIntExact(entero(cuenta, "id_catalogo_cuenta"));
    }

    /** Valida identidad, vigencia, comprobante, pago asociado y distribución de medios. */
    private Origen validar(Map<String, Object> operacion, boolean bloquear) {
        if (operacion == null
                || !"CARTERA".equals(texto(operacion, "modulo_origen"))
                || !"PRORROGA".equals(texto(operacion, "proceso_origen"))
                || !"cartera.creditos_prorrogas".equals(texto(operacion, "tabla_origen"))
                || !"ENTRADA".equals(texto(operacion, "naturaleza"))) {
            throw new IllegalArgumentException("El origen no corresponde a un recaudo de prórroga.");
        }
        Long idProrroga = entero(operacion, "id_origen");
        Integer agencia = Math.toIntExact(entero(operacion, "id_agencia"));
        BigDecimal publicado = importe(operacion, "valor_original");
        if (idProrroga <= 0 || agencia <= 0 || publicado.signum() <= 0) {
            throw new IllegalStateException("La operación de prórroga contiene datos inválidos.");
        }
        List<Map<String, Object>> prorrogas = jdbcTemplate.queryForList("""
                SELECT id_credito_prorroga, id_cartera_credito, id_agencia,
                       estado_prorroga, tipo_comprobante, numero_comprobante
                FROM cartera.creditos_prorrogas
                WHERE id_credito_prorroga = ?
                """ + (bloquear ? " FOR UPDATE" : ""), idProrroga);
        if (prorrogas.size() != 1) {
            throw new IllegalStateException("No existe la prórroga de origen.");
        }
        Map<String, Object> prorroga = prorrogas.get(0);
        String numeroCC = texto(prorroga, "numero_comprobante");
        if (!"A".equals(texto(prorroga, "estado_prorroga"))
                || !"CC".equals(texto(prorroga, "tipo_comprobante"))
                || numeroCC.isBlank()
                || entero(prorroga, "id_agencia") != agencia.longValue()) {
            throw new IllegalStateException("La prórroga no está vigente o su comprobante/agencia es inválido.");
        }
        Long credito = entero(prorroga, "id_cartera_credito");
        List<Map<String, Object>> pagos = jdbcTemplate.queryForList("""
                SELECT id_pago, id_cartera_credito, id_agencia_receptora,
                       estado, tipo_operacion, tipo_comprobante, numero_comprobante,
                       valor_pago, medios_recaudo::text AS medios_recaudo,
                       liquidacion::text AS liquidacion
                FROM cartera.pagos_cuotas
                WHERE id_cartera_credito = ?
                  AND id_agencia_receptora = ?
                  AND tipo_operacion = 'PRORROGA'
                  AND tipo_comprobante = 'CC'
                  AND TRIM(numero_comprobante) = ?
                  AND liquidacion ->> 'idCreditoProrroga' = ?
                """ + (bloquear ? " FOR UPDATE" : ""),
                credito, agencia, numeroCC, idProrroga.toString());
        if (pagos.size() != 1) {
            throw new IllegalStateException("La prórroga no tiene un único pago asociado a su CC.");
        }
        Map<String, Object> pago = pagos.get(0);
        if (!"APLICADO".equals(texto(pago, "estado"))) {
            throw new IllegalStateException("El pago de la prórroga no está aplicado.");
        }
        BigDecimal totalMedios = BigDecimal.ZERO;
        BigDecimal totalCxC = BigDecimal.ZERO;
        try {
            JsonNode medios = objectMapper.readTree(texto(pago, "medios_recaudo"));
            if (medios == null || !medios.isArray() || medios.isEmpty()) {
                throw new IllegalStateException("La prórroga no tiene medios de recaudo válidos.");
            }
            for (JsonNode medio : medios) {
                String clase = medio.path("tipoMedio").asText("");
                JsonNode valor = medio.path("valor");
                if (!("CAJA".equals(clase) || "AHORROS".equals(clase) || "CXP".equals(clase))
                        || !valor.isNumber() || valor.decimalValue().signum() <= 0) {
                    throw new IllegalStateException("La prórroga contiene un medio de recaudo inválido.");
                }
                totalMedios = totalMedios.add(valor.decimalValue());
                if ("CXP".equals(clase)) {
                    totalCxC = totalCxC.add(valor.decimalValue());
                }
            }
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No fue posible interpretar los medios de la prórroga.", ex);
        }
        if (totalMedios.compareTo(importe(pago, "valor_pago")) != 0
                || totalCxC.compareTo(publicado) != 0) {
            throw new IllegalStateException("El pago y la CxC publicada no coinciden con los medios originales.");
        }
        Integer origenes = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM contabilidad.origen_comprobantes
                WHERE id_agencia = ? AND tipo_comprobante = 'CC'
                  AND TRIM(numero_comprobante) = ?
                  AND origen_tipo = 'AUTO' AND estado_origen = 'ACTIVO'
                  AND modulo_origen = 'CARTERA'
                  AND proceso_origen = 'PRORROGA'
                  AND tabla_origen = 'cartera.creditos_prorrogas'
                  AND id_origen = ?
                """, Integer.class, agencia, numeroCC, idProrroga);
        if (origenes == null || origenes != 1) {
            throw new IllegalStateException("El CC original no corresponde a la prórroga indicada.");
        }
        return new Origen(agencia, numeroCC, publicado);
    }

    private record Origen(Integer agencia, String numeroComprobante, BigDecimal valorCxC) { }

    private static String texto(Map<String, Object> fila, String campo) {
        return Objects.toString(fila.get(campo), "").trim();
    }

    private static Long entero(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);
        if (!(valor instanceof Number numero)) {
            throw new IllegalStateException("Falta el identificador obligatorio: " + campo);
        }
        return numero.longValue();
    }

    private static BigDecimal importe(Map<String, Object> fila, String campo) {
        Object valor = fila.get(campo);
        if (!(valor instanceof Number numero)) {
            throw new IllegalStateException("Falta el importe obligatorio: " + campo);
        }
        return valor instanceof BigDecimal decimal ? decimal : new BigDecimal(numero.toString());
    }
}
