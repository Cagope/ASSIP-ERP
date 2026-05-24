package co.assip.erp.cdat.liquidacion_diaria;

import co.assip.erp.cdat.liquidacion_diaria.dto.CdatLiquidacionDiariaItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CdatLiquidacionDiariaRepository {

    private final JdbcTemplate jdbc;

    public boolean existeLiquidacion(Integer idAgencia, LocalDate fechaLiquidacion) {
        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM cdat.liquidaciones_diarias_control
            WHERE id_agencia = ?
              AND fecha_liquidacion = ?
              AND estado = 'PROCESADO'
        """, Integer.class, idAgencia, fechaLiquidacion);

        return count != null && count > 0;
    }

    public List<CdatLiquidacionDiariaItemDTO> listarCdatsLiquidables(
            Integer idAgencia,
            LocalDate fechaLiquidacion
    ) {
        return jdbc.query("""
        SELECT
            c.id_cuenta_cdat,
            c.id_agencia,
            c.codigo_cdat,
            c.id_datos_personal,
            hv.documento,
            CASE
                WHEN hv.tipo_persona = '2' THEN COALESCE(hv.nombres, '')
                ELSE TRIM(CONCAT_WS(' ',
                    hv.primer_apellido,
                    hv.segundo_apellido,
                    hv.nombres
                ))
            END AS nombre_completo,
            c.fecha_proxima_liquidacion,
            c.fecha_proximo_traslado_interes,
            c.plazo_meses,
            c.saldo_actual_cdat,
            c.tasa_nominal_anual,
            c.retencion_fuente_cdat,
            c.id_cuenta_ahorro,
            ca.codigo_cuenta AS codigo_cuenta_ahorro,
            fa.codigo_forma AS codigo_forma_ahorro,
            fa.nombre_forma AS nombre_forma_ahorro
        FROM cdat.cuentas_cdats c
        LEFT JOIN LATERAL (
            SELECT
                hv.documento,
                hv.tipo_persona,
                hv.nombres,
                hv.primer_apellido,
                hv.segundo_apellido
            FROM reporting.vw_hoja_vida_general_total_extendida hv
            WHERE hv.id_datos_personal = c.id_datos_personal
            LIMIT 1
        ) hv ON true
        LEFT JOIN depositos.cuentas_ahorro ca
               ON ca.id_cuenta_ahorro = c.id_cuenta_ahorro
        LEFT JOIN depositos.formas_ahorro fa
               ON fa.id_forma_ahorro = ca.id_forma_ahorro
        WHERE c.id_agencia = ?
          AND c.estado_cdat = 'A'
          AND COALESCE(c.saldo_actual_cdat, 0) > 0
          AND c.fecha_proxima_liquidacion = ?
        ORDER BY c.codigo_cdat
    """, (rs, rowNum) -> CdatLiquidacionDiariaItemDTO.builder()
                .idCuentaCdat(rs.getLong("id_cuenta_cdat"))
                .idAgencia(rs.getInt("id_agencia"))
                .codigoCdat(rs.getString("codigo_cdat"))
                .idDatosPersonal(rs.getInt("id_datos_personal"))
                .documento(rs.getString("documento"))
                .nombreCompleto(rs.getString("nombre_completo"))
                .fechaLiquidacion(rs.getObject("fecha_proxima_liquidacion", LocalDate.class))
                .fechaProximoTraslado(rs.getObject("fecha_proximo_traslado_interes", LocalDate.class))
                .plazoMeses(rs.getInt("plazo_meses"))
                .capitalCdat(nvl(rs.getBigDecimal("saldo_actual_cdat")))
                .tasaNominalAnual(nvl(rs.getBigDecimal("tasa_nominal_anual")))
                .aplicaRetencion((Boolean) rs.getObject("retencion_fuente_cdat"))
                .idCuentaAhorro((Integer) rs.getObject("id_cuenta_ahorro"))
                .codigoCuentaAhorro(rs.getString("codigo_cuenta_ahorro"))
                .codigoFormaAhorro(rs.getString("codigo_forma_ahorro"))
                .nombreFormaAhorro(rs.getString("nombre_forma_ahorro"))
                .build(), idAgencia, fechaLiquidacion);
    }

    public BigDecimal obtenerValorBaseRetencion(Integer idAgencia) {
        return obtenerParametroNumerico(idAgencia, "501");
    }

    public BigDecimal obtenerPorcentajeRetencion(Integer idAgencia) {
        return obtenerParametroNumerico(idAgencia, "502");
    }

    private BigDecimal obtenerParametroNumerico(Integer idAgencia, String codigo) {
        List<BigDecimal> lista = jdbc.query("""
            SELECT valor_parametro::numeric
            FROM general.parametros
            WHERE id_agencia = ?
              AND CAST(codigo_parametro AS TEXT) = ?
            LIMIT 1
        """, (rs, rowNum) -> rs.getBigDecimal(1), idAgencia, codigo);

        return lista.isEmpty() || lista.get(0) == null
                ? BigDecimal.ZERO
                : lista.get(0);
    }

    public Integer obtenerCuentaConcepto(Integer idAgencia, String codigoProceso) {
        List<Integer> lista = jdbc.query("""
            SELECT
                CASE
                    WHEN id_catalogo_cuenta_debito IS NOT NULL THEN id_catalogo_cuenta_debito
                    WHEN id_catalogo_cuenta_credito IS NOT NULL THEN id_catalogo_cuenta_credito
                    ELSE NULL
                END AS cuenta
            FROM cdat.conceptos_contables_cdats
            WHERE codigo_proceso = ?
              AND activo = TRUE
              AND (id_agencia IS NULL OR id_agencia = ?)
            ORDER BY id_agencia DESC
            LIMIT 1
        """, (rs, rowNum) -> (Integer) rs.getObject("cuenta"), codigoProceso, idAgencia);

        if (lista.isEmpty() || lista.get(0) == null) {
            throw new RuntimeException("No existe concepto contable CDAT: " + codigoProceso);
        }

        return lista.get(0);
    }

    public void insertarExtractoCdat(
            Long idCuentaCdat,
            LocalDate fecha,
            String tipoComprobante,
            String numeroComprobante,
            String tipoMovimiento,
            BigDecimal debito,
            BigDecimal credito,
            Integer idUsuario
    ) {
        jdbc.update("""
            INSERT INTO cdat.extractos_cuentas_cdats (
                id_cuenta_cdat,
                fecha_movimiento,
                hora_movimiento,
                tipo_comprobante,
                numero_comprobante,
                tipo_movimiento,
                valor_debito,
                valor_credito,
                modulo,
                tarjeta,
                fk_seguridad_creacion,
                fecha_creacion
            )
            VALUES (?, ?, CURRENT_TIME, ?, ?, ?, ?, ?, '11', 'N', ?, CURRENT_TIMESTAMP)
        """,
                idCuentaCdat,
                fecha,
                tipoComprobante,
                numeroComprobante,
                tipoMovimiento,
                nvl(debito),
                nvl(credito),
                idUsuario
        );
    }

    public void actualizarFechasCdat(
            Long idCuentaCdat,
            LocalDate fechaLiquidacion,
            LocalDate proximaLiquidacion,
            LocalDate proximoTraslado,
            Integer idUsuario
    ) {
        jdbc.update("""
            UPDATE cdat.cuentas_cdats
               SET fecha_ultima_liquidacion = ?,
                   fecha_proxima_liquidacion = ?,
                   fecha_proximo_traslado_interes = COALESCE(?, fecha_proximo_traslado_interes),
                   fk_seguridad_edicion = ?,
                   fecha_edicion = CURRENT_TIMESTAMP
             WHERE id_cuenta_cdat = ?
        """, fechaLiquidacion, proximaLiquidacion, proximoTraslado, idUsuario, idCuentaCdat);
    }

    public Integer obtenerMesesAmortizacion(String codigoAmortizacion) {
        List<Integer> lista = jdbc.query("""
            SELECT meses
            FROM cdat.amortizaciones_cdats
            WHERE codigo_amortizacion = ?
            LIMIT 1
        """, (rs, rowNum) -> rs.getInt("meses"), codigoAmortizacion);

        return lista.isEmpty() ? 1 : lista.get(0);
    }

    public String obtenerAmortizacionCdat(Long idCuentaCdat) {
        return jdbc.queryForObject("""
            SELECT amortizacion_deposito
            FROM cdat.cuentas_cdats
            WHERE id_cuenta_cdat = ?
        """, String.class, idCuentaCdat);
    }

    public void insertarControl(
            Integer idAgencia,
            LocalDate fechaLiquidacion,
            LocalDate fechaContable,
            String tipoComprobante,
            String numeroComprobante,
            Integer totalCdats,
            BigDecimal totalInteres,
            BigDecimal totalRetencion,
            BigDecimal totalNeto,
            BigDecimal totalTrasladado,
            BigDecimal totalNoTrasladado,
            Integer idUsuario
    ) {
        jdbc.update("""
            INSERT INTO cdat.liquidaciones_diarias_control (
                id_agencia,
                fecha_liquidacion,
                fecha_contable,
                tipo_comprobante,
                numero_comprobante,
                total_cdats,
                total_interes,
                total_retencion,
                total_neto,
                total_trasladado_depositos,
                total_no_trasladado,
                estado,
                fk_seguridad_creacion,
                fecha_creacion
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PROCESADO', ?, CURRENT_TIMESTAMP)
        """,
                idAgencia,
                fechaLiquidacion,
                fechaContable,
                tipoComprobante,
                numeroComprobante,
                totalCdats,
                totalInteres,
                totalRetencion,
                totalNeto,
                totalTrasladado,
                totalNoTrasladado,
                idUsuario
        );
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}