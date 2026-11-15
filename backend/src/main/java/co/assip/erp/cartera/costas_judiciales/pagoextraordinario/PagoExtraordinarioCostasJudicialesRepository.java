package co.assip.erp.cartera.costas_judiciales.pagoextraordinario;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Consulta y registra disminuciones de costas; usa el mismo saldo de Administración. */
@Repository
@RequiredArgsConstructor
public class PagoExtraordinarioCostasJudicialesRepository {
    private final NamedParameterJdbcTemplate jdbc;

    /** Obtiene crédito y cabecera activa sin bloquear para consultas y vista previa. */
    public Optional<DatosCostas> consultarCostas(Integer idCredito) {
        List<DatosCostas> datos = jdbc.query("""
                SELECT cr.id_cartera_credito, cr.id_agencia, cr.id_datos_personal AS id_asociado,
                       cj.id_credito_costa_judicial
                FROM cartera.carteras_creditos cr
                JOIN cartera.creditos_costas_judiciales cj
                  ON cj.id_cartera_credito = cr.id_cartera_credito AND cj.activo = TRUE
                WHERE cr.id_cartera_credito = :credito
                ORDER BY cj.id_credito_costa_judicial
                """, new MapSqlParameterSource("credito", idCredito),
                (rs, i) -> new DatosCostas(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getLong(4)));
        if (datos.size() > 1) throw new IllegalStateException("Existen varias cabeceras activas de costas.");
        return datos.stream().findFirst();
    }

    /** Serializa con Administración bloqueando primero el crédito y luego su cabecera. */
    @Transactional(propagation = Propagation.MANDATORY)
    public DatosCostas bloquearCostas(Integer idCredito) {
        List<Integer> creditos = jdbc.query("""
                SELECT id_cartera_credito FROM cartera.carteras_creditos
                WHERE id_cartera_credito = :credito FOR UPDATE
                """, new MapSqlParameterSource("credito", idCredito), (rs, i) -> rs.getInt(1));
        if (creditos.isEmpty()) throw new IllegalArgumentException("El crédito no existe.");
        jdbc.query("""
                SELECT id_credito_costa_judicial FROM cartera.creditos_costas_judiciales
                WHERE id_cartera_credito = :credito AND activo = TRUE
                ORDER BY id_credito_costa_judicial FOR UPDATE
                """, new MapSqlParameterSource("credito", idCredito), (rs, i) -> rs.getLong(1));
        return consultarCostas(idCredito).orElseThrow(() ->
                new IllegalArgumentException("El crédito no tiene costas judiciales activas."));
    }

    /** Suma cargos menos disminuciones de la cabecera activa. */
    public BigDecimal consultarSaldo(Long cabecera) {
        return jdbc.queryForObject("""
                SELECT COALESCE(SUM(valor_debito - valor_credito), 0)
                FROM cartera.creditos_costas_judiciales_detalle
                WHERE id_credito_costa_judicial = :cabecera AND estado = 'A'
                """, new MapSqlParameterSource("cabecera", cabecera), BigDecimal.class);
    }

    /**
     * Lista únicamente causaciones originales, de la más reciente a la más antigua.
     * Las reversiones de pagos son débitos que restablecen el saldo, no cargos nuevos.
     * Una contrapartida solo se considera verificable si el origen contable identifica
     * expresamente la causación y existe una única partida de crédito por su valor.
     * Las causaciones históricas sin trazabilidad se conservan en el resultado con
     * contrapartida nula para permitir el tratamiento parametrizado en el servicio.
     */
    public List<Cargo> listarCargos(Integer idCredito) {
        return jdbc.query("""
                SELECT d.id_credito_costa_judicial_detalle, d.fecha_movimiento,
                       d.valor_debito, d.observacion_costa,
                       contra.id_catalogo_cuenta, contra.id_datos_personal,
                       cc.codigo_cuenta, cc.nombre_cuenta,
                       COALESCE(dp.documento, '') AS documento_tercero,
                       COALESCE((
                           SELECT SUM(
                               split_part(split_part(x.observacion_costa, ':MONTO:', 2), ' |', 1)::numeric
                           )
                           FROM cartera.creditos_costas_judiciales_detalle x
                           WHERE x.id_credito_costa_judicial = d.id_credito_costa_judicial
                             AND x.estado = 'A'
                             AND x.tipo_movimiento = 'C'
                             AND x.observacion_costa LIKE
                                 ('CORRECCION_CARGO:' || d.id_credito_costa_judicial_detalle || ':MONTO:% |%')
                       ), 0) AS corregido
                FROM cartera.creditos_costas_judiciales cj
                JOIN cartera.carteras_creditos cr
                  ON cr.id_cartera_credito = cj.id_cartera_credito
                JOIN cartera.creditos_costas_judiciales_detalle d
                  ON d.id_credito_costa_judicial = cj.id_credito_costa_judicial
                LEFT JOIN LATERAL (
                    SELECT a.id_catalogo_cuenta, a.id_datos_personal
                    FROM contabilidad.origen_comprobantes o
                    JOIN contabilidad.auxiliares_contables a
                      ON a.id_agencia = o.id_agencia
                     AND TRIM(a.tipo_comprobante) = TRIM(o.tipo_comprobante)
                     AND TRIM(a.numero_comprobante) = TRIM(o.numero_comprobante)
                     AND a.valor_credito = d.valor_debito
                    WHERE o.tabla_origen = 'cartera.creditos_costas_judiciales_detalle'
                      AND o.id_origen = d.id_credito_costa_judicial_detalle
                      AND o.id_agencia = cr.id_agencia
                      AND o.modulo_origen = 'CARTERA'
                      AND o.proceso_origen = 'COSTAS_JUDICIALES'
                      AND (SELECT COUNT(*)
                            FROM contabilidad.auxiliares_contables ax
                            WHERE ax.id_agencia = o.id_agencia
                              AND TRIM(ax.tipo_comprobante) = TRIM(o.tipo_comprobante)
                              AND TRIM(ax.numero_comprobante) = TRIM(o.numero_comprobante)
                              AND ax.valor_credito > 0) = 1
                ) contra ON TRUE
                LEFT JOIN contabilidad.catalogo_cuentas cc
                  ON cc.id_catalogo_cuenta = contra.id_catalogo_cuenta
                 AND cc.id_agencia = cr.id_agencia
                LEFT JOIN hoja_vida.datos_personales dp
                  ON dp.id_datos_personal = contra.id_datos_personal
                WHERE cj.id_cartera_credito = :credito
                  AND cj.activo = TRUE
                  AND d.estado = 'A'
                  AND d.tipo_movimiento = 'D'
                  AND COALESCE(d.observacion_costa, '') NOT ILIKE 'Reversión del pago%'
                ORDER BY d.fecha_movimiento DESC, d.id_credito_costa_judicial_detalle DESC
                """, new MapSqlParameterSource("credito", idCredito), (rs, i) ->
                new Cargo(rs.getLong(1), rs.getDate(2).toLocalDate(), rs.getBigDecimal(3),
                        rs.getString(4), (Integer) rs.getObject(5), (Integer) rs.getObject(6),
                        rs.getString(7), rs.getString(8), rs.getString(9), rs.getBigDecimal(10)));
    }

    /** Lista las cuentas de ahorros operativas del asociado y de la agencia del crédito. */
    public List<CuentaAhorro> listarCuentasAhorro(Integer idCredito) {
        return jdbc.query("""
                SELECT a.id_cuenta_ahorro, a.id_agencia, TRIM(a.codigo_cuenta),
                       f.nombre_forma, a.saldo_actual_cuenta
                FROM cartera.carteras_creditos cr
                JOIN depositos.cuentas_ahorro a ON a.id_datos_personal = cr.id_datos_personal
                   AND a.id_agencia = cr.id_agencia
                JOIN depositos.formas_ahorro f ON f.id_forma_ahorro = a.id_forma_ahorro
                   AND f.id_agencia = a.id_agencia
                WHERE cr.id_cartera_credito = :credito
                  AND TRIM(f.tipo_captacion_forma) = '2'
                  AND TRIM(a.estado_cuenta_cuenta) = 'A'
                  AND EXISTS (SELECT 1 FROM depositos.estados_ahorros ea
                              WHERE TRIM(ea.codigo_estado_ahorro) = TRIM(a.cuenta_activa)
                                AND ea.operativo = TRUE)
                ORDER BY f.codigo_forma, a.codigo_cuenta, a.id_cuenta_ahorro
                """, new MapSqlParameterSource("credito", idCredito), (rs, i) ->
                new CuentaAhorro(rs.getInt(1), rs.getInt(2), rs.getString(3),
                        rs.getString(4), rs.getBigDecimal(5)));
    }

    /** Registra una sola disminución bajo bloqueo previo; el cargo original no se altera. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Long registrarDisminucion(DatosCostas costas, LocalDate fecha, BigDecimal valor,
                                     String numero, String observacion, Integer usuario) {
        if (costas == null || fecha == null || valor == null || valor.signum() <= 0
                || numero == null || numero.isBlank() || observacion == null
                || observacion.isBlank() || usuario == null || usuario <= 0)
            throw new IllegalArgumentException("Datos incompletos de disminución de costas.");
        if (valor.compareTo(consultarSaldo(costas.idCabecera())) > 0)
            throw new IllegalArgumentException("El valor supera el saldo pendiente de costas.");
        return jdbc.queryForObject("""
                INSERT INTO cartera.creditos_costas_judiciales_detalle (
                    id_credito_costa_judicial, fecha_movimiento, tipo_movimiento,
                    valor_debito, valor_credito, tipo_comprobante, numero_comprobante,
                    observacion_costa, estado, fk_seguridad_creacion, fk_seguridad_edicion
                ) VALUES (:cabecera, :fecha, 'C', 0, :valor, 'CC', :numero,
                          :observacion, 'A', :usuario, :usuario)
                RETURNING id_credito_costa_judicial_detalle
                """, new MapSqlParameterSource().addValue("cabecera", costas.idCabecera())
                .addValue("fecha", fecha).addValue("valor", valor).addValue("numero", numero)
                .addValue("observacion", observacion).addValue("usuario", usuario), Long.class);
    }

    public record DatosCostas(Integer idCredito, Integer idAgencia, Integer idAsociado, Long idCabecera) { }
    public record CuentaAhorro(Integer idCuentaAhorro, Integer idAgencia, String codigoCuenta,
                               String nombreForma, BigDecimal saldoDisponible) { }
    public record Cargo(Long idDetalleCargo, LocalDate fecha, BigDecimal valor, String observacion,
                        Integer idCuentaContrapartida, Integer idTercero, String codigoCuenta,
                        String nombreCuenta, String documentoTercero, BigDecimal valorCorregido) { }
}
