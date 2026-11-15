package co.assip.erp.tesoreria.operaciones_pendientes;

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
public class TesoreriaOrigenSegurosValidator implements TesoreriaOrigenFinancieroValidator {
    private final JdbcTemplate jdbcTemplate;

    /** Reconoce exclusivamente el pago extraordinario de Seguros de Cartera. */
    @Override
    public boolean soporta(Map<String, Object> operacion) {
        return operacion != null
                && "CARTERA".equals(texto(operacion, "modulo_origen"))
                && "PAGO_EXTRAORDINARIO_SEGUROS".equals(texto(operacion, "proceso_origen"))
                && "cartera.creditos_seguros_detalle".equals(texto(operacion, "tabla_origen"));
    }

    /** Verifica la vigencia del movimiento original sin modificarlo. */
    @Override
    @Transactional(readOnly = true)
    public void validarVistaPrevia(Map<String, Object> operacion) {
        validarVigencia(operacion);
        consultarCuentaCxCOriginal(operacion);
    }

    /** Bloquea el origen y revalida su respaldo en la transacción de confirmación. */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void validarConfirmacion(Map<String, Object> operacion) {
        bloquearYValidarVigencia(operacion);
        consultarCuentaCxCOriginal(operacion);
    }

    /** Obtiene la CxC debitada en el comprobante automático original, sin consultar parámetros actuales. */
    @Override
    @Transactional(readOnly = true)
    public Integer obtenerCuentaCxCOriginal(Map<String, Object> operacion) {
        validarVigencia(operacion);
        return consultarCuentaCxCOriginal(operacion);
    }

    /** Bloquea el movimiento de origen y comprueba su vigencia durante la confirmación. */
    private void bloquearYValidarVigencia(Map<String, Object> operacion) {
        exigirOrigen(operacion);
        List<Map<String, Object>> filas = jdbcTemplate.queryForList(
                "SELECT id_credito_seguro_detalle FROM cartera.creditos_seguros_detalle "
                        + "WHERE id_credito_seguro_detalle = ? AND estado = 'A' FOR UPDATE",
                numeroLong(operacion, "id_origen"));
        if (filas.size() != 1) {
            throw new IllegalStateException("El movimiento extraordinario original no está vigente.");
        }
    }

    /** Conserva la comprobación de estado A que utilizaba el servicio central. */
    private void validarVigencia(Map<String, Object> operacion) {
        exigirOrigen(operacion);
        Integer vigentes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cartera.creditos_seguros_detalle "
                        + "WHERE id_credito_seguro_detalle = ? AND estado = 'A'",
                Integer.class, numeroLong(operacion, "id_origen"));
        if (vigentes == null || vigentes != 1) {
            throw new IllegalStateException("El movimiento extraordinario original no está vigente.");
        }
    }

    /** Replica el respaldo histórico y la igualdad exacta de importes del servicio vigente. */
    private Integer consultarCuentaCxCOriginal(Map<String, Object> operacion) {
        exigirOrigen(operacion);
        List<Map<String, Object>> cuentas = jdbcTemplate.queryForList("""
                SELECT ac.id_catalogo_cuenta, SUM(ac.valor_debito) AS total_debito
                FROM contabilidad.origen_comprobantes oc
                JOIN contabilidad.auxiliares_contables ac
                  ON ac.id_agencia = oc.id_agencia
                 AND ac.tipo_comprobante = oc.tipo_comprobante
                 AND ac.numero_comprobante = oc.numero_comprobante
                JOIN contabilidad.catalogo_cuentas cc
                  ON cc.id_catalogo_cuenta = ac.id_catalogo_cuenta
                 AND cc.id_agencia = ac.id_agencia
                WHERE oc.id_agencia = ?
                  AND oc.origen_tipo = 'AUTO'
                  AND oc.estado_origen = 'ACTIVO'
                  AND oc.modulo_origen = 'CARTERA'
                  AND oc.proceso_origen = ?
                  AND oc.tabla_origen = ?
                  AND oc.id_origen = ?
                  AND ac.estado_movimiento = 'A'
                  AND ac.detalle_movimiento = 'BANCOS / CXC'
                  AND ac.valor_debito > 0
                  AND ac.valor_credito = 0
                  AND cc.cuenta_operable = TRUE
                GROUP BY ac.id_catalogo_cuenta
                """, numeroEntero(operacion, "id_agencia"),
                texto(operacion, "proceso_origen"), texto(operacion, "tabla_origen"),
                numeroLong(operacion, "id_origen"));
        if (cuentas.size() != 1) {
            throw new IllegalStateException(
                    "No se encontró una única cuenta CxC bancaria en el comprobante original.");
        }
        Map<String, Object> cuenta = cuentas.get(0);
        if (numeroDecimal(cuenta, "total_debito").compareTo(
                numeroDecimal(operacion, "valor_original")) != 0) {
            throw new IllegalStateException(
                    "El valor de la CxC original no coincide con la obligación publicada.");
        }
        return numeroEntero(cuenta, "id_catalogo_cuenta");
    }

    /** Impide utilizar este validador con un proceso o tabla diferente. */
    private void exigirOrigen(Map<String, Object> operacion) {
        if (!soporta(operacion)) {
            throw new IllegalStateException("El origen requiere un validador financiero específico.");
        }
    }

    private String texto(Map<String, Object> datos, String clave) {
        return Objects.toString(datos.get(clave), "").trim();
    }

    private Long numeroLong(Map<String, Object> datos, String clave) {
        Object valor = datos.get(clave);
        if (!(valor instanceof Number numero)) {
            throw new IllegalStateException("El origen no contiene un identificador numérico válido: " + clave);
        }
        return numero.longValue();
    }

    private Integer numeroEntero(Map<String, Object> datos, String clave) {
        return Math.toIntExact(numeroLong(datos, clave));
    }

    private BigDecimal numeroDecimal(Map<String, Object> datos, String clave) {
        Object valor = datos.get(clave);
        if (!(valor instanceof Number numero)) {
            throw new IllegalStateException("El origen no contiene un importe válido: " + clave);
        }
        return new BigDecimal(numero.toString());
    }
}
