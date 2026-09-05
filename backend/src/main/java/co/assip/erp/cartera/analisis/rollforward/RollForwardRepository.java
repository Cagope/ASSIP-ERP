package co.assip.erp.cartera.analisis.rollforward;

import co.assip.erp.cartera.analisis.rollforward.dto.*;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class RollForwardRepository {

    private final JdbcClient jdbcClient;

    public RollForwardRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<RollForwardCorteDTO> listarCortes() {
        return jdbcClient.sql("""
                SELECT fecha_corte_anterior, fecha_corte
                FROM cartera.vw_roll_forward_resumen
                ORDER BY fecha_corte DESC
                """)
                .query((rs, n) -> new RollForwardCorteDTO(
                        date(rs, "fecha_corte_anterior"),
                        date(rs, "fecha_corte")))
                .list();
    }

    public Optional<RollForwardResumenDTO> buscarResumen(LocalDate fechaCorte) {
        return jdbcClient.sql("""
                SELECT *
                FROM cartera.vw_roll_forward_resumen
                WHERE fecha_corte = :fechaCorte
                """)
                .param("fechaCorte", fechaCorte)
                .query(RESUMEN)
                .optional();
    }

    public List<RollForwardResumenDTO> listarHistoricoResumen() {
        return jdbcClient.sql("""
                SELECT *
                FROM cartera.vw_roll_forward_resumen
                ORDER BY fecha_corte DESC
                """).query(RESUMEN).list();
    }

    public List<RollForwardLineaDTO> listarLineas(LocalDate fechaCorte) {
        return jdbcClient.sql("""
                SELECT *
                FROM cartera.vw_roll_forward_lineas
                WHERE fecha_corte = :fechaCorte
                ORDER BY saldo_final DESC, codigo_linea_credito
                """)
                .param("fechaCorte", fechaCorte)
                .query(LINEA)
                .list();
    }

    public List<String> listarTiposMovimiento(LocalDate fechaCorte) {
        return jdbcClient.sql("""
                SELECT DISTINCT tipo_movimiento
                FROM cartera.vw_roll_forward_detalle
                WHERE fecha_corte = :fechaCorte
                  AND tipo_movimiento IS NOT NULL
                ORDER BY tipo_movimiento
                """)
                .param("fechaCorte", fechaCorte)
                .query(String.class)
                .list();
    }

    public List<RollForwardDetalleDTO> listarDetalle(
            LocalDate fechaCorte, String tipoMovimiento, Long idLineaCredito) {

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM cartera.vw_roll_forward_detalle
                WHERE fecha_corte = :fechaCorte
                """);

        if (tipoMovimiento != null) {
            sql.append(" AND tipo_movimiento = :tipoMovimiento ");
        }
        if (idLineaCredito != null) {
            sql.append(" AND id_linea_credito = :idLineaCredito ");
        }

        sql.append("""
                ORDER BY tipo_movimiento,
                         saldo_anterior DESC,
                         saldo_actual DESC,
                         pagare_cartera
                """);

        var spec = jdbcClient.sql(sql.toString()).param("fechaCorte", fechaCorte);
        if (tipoMovimiento != null) spec = spec.param("tipoMovimiento", tipoMovimiento);
        if (idLineaCredito != null) spec = spec.param("idLineaCredito", idLineaCredito);

        return spec.query(DETALLE).list();
    }

    private static final RowMapper<RollForwardResumenDTO> RESUMEN = (rs, n) ->
            new RollForwardResumenDTO(
                    date(rs,"fecha_corte_anterior"), date(rs,"fecha_corte"),
                    integer(rs,"creditos_iniciales"), integer(rs,"creditos_finales"),
                    integer(rs,"creditos_nuevos"), integer(rs,"creditos_reingresados"),
                    integer(rs,"creditos_reduccion"), integer(rs,"creditos_sin_variacion"),
                    integer(rs,"creditos_aumento"), integer(rs,"creditos_prepago"),
                    integer(rs,"creditos_cancelacion_normal"),
                    integer(rs,"creditos_cancelacion_post_vencimiento"),
                    integer(rs,"creditos_ausencia_temporal"), integer(rs,"creditos_otras_salidas"),
                    dec(rs,"saldo_inicial"), dec(rs,"nuevos"), dec(rs,"reingresos"),
                    dec(rs,"aumentos_saldo"), dec(rs,"reducciones_saldo"), dec(rs,"prepagos"),
                    dec(rs,"cancelaciones_normales"), dec(rs,"cancelaciones_post_vencimiento"),
                    dec(rs,"ausencias_temporales"), dec(rs,"otras_salidas"), dec(rs,"saldo_final"),
                    dec(rs,"total_entradas"), dec(rs,"total_salidas_reducciones"),
                    dec(rs,"variacion_neta"), dec(rs,"variacion_porcentaje"),
                    dec(rs,"tasa_nuevos_sobre_saldo_inicial"),
                    dec(rs,"tasa_reduccion_sobre_saldo_inicial"),
                    dec(rs,"tasa_prepago_sobre_saldo_inicial"),
                    dec(rs,"tasa_salidas_definitivas_sobre_saldo_inicial"),
                    dec(rs,"saldo_final_calculado"), dec(rs,"diferencia_control"),
                    integer(rs,"creditos_finales_calculados"),
                    integer(rs,"diferencia_control_creditos"));

    private static final RowMapper<RollForwardLineaDTO> LINEA = (rs, n) ->
            new RollForwardLineaDTO(
                    date(rs,"fecha_corte_anterior"), date(rs,"fecha_corte"),
                    lng(rs,"id_linea_credito"), rs.getString("codigo_linea_credito"),
                    rs.getString("nombre_linea_credito"),
                    integer(rs,"creditos_iniciales"), integer(rs,"creditos_finales"),
                    integer(rs,"creditos_nuevos"), integer(rs,"creditos_reingresados"),
                    integer(rs,"creditos_reduccion"), integer(rs,"creditos_sin_variacion"),
                    integer(rs,"creditos_aumento"), integer(rs,"creditos_prepago"),
                    integer(rs,"creditos_cancelacion_normal"),
                    integer(rs,"creditos_cancelacion_post_vencimiento"),
                    integer(rs,"creditos_ausencia_temporal"), integer(rs,"creditos_otras_salidas"),
                    dec(rs,"saldo_inicial"), dec(rs,"nuevos"), dec(rs,"reingresos"),
                    dec(rs,"aumentos_saldo"), dec(rs,"reducciones_saldo"), dec(rs,"prepagos"),
                    dec(rs,"cancelaciones_normales"), dec(rs,"cancelaciones_post_vencimiento"),
                    dec(rs,"ausencias_temporales"), dec(rs,"otras_salidas"), dec(rs,"saldo_final"),
                    dec(rs,"variacion_neta"), dec(rs,"variacion_porcentaje"),
                    dec(rs,"saldo_final_calculado"), dec(rs,"diferencia_control"),
                    integer(rs,"creditos_finales_calculados"),
                    integer(rs,"diferencia_control_creditos"));

    private static final RowMapper<RollForwardDetalleDTO> DETALLE = (rs, n) ->
            new RollForwardDetalleDTO(
                    date(rs,"fecha_corte_anterior"), date(rs,"fecha_corte"),
                    lng(rs,"id_cierre_cartera_anterior"), lng(rs,"id_cierre_cartera_actual"),
                    lng(rs,"id_cierre_cartera_credito_anterior"), lng(rs,"id_cierre_cartera_credito_actual"),
                    lng(rs,"id_cartera_credito"), lng(rs,"id_agencia"),
                    lng(rs,"id_linea_credito"), rs.getString("codigo_linea_credito"),
                    rs.getString("nombre_linea_credito"), rs.getString("pagare_cartera"),
                    lng(rs,"id_datos_personal"), rs.getString("tipo_documento"),
                    rs.getString("documento"), rs.getString("nombre_completo"),
                    rs.getString("codigo_clasificacion_credito"),
                    rs.getString("descripcion_clasificacion_credito"),
                    rs.getString("codigo_destino_economico"),
                    rs.getString("descripcion_destino_economico"),
                    dec(rs,"valor_inicial_credito"), dec(rs,"valor_desembolsado"),
                    date(rs,"fecha_desembolso"), date(rs,"fecha_final"),
                    integer(rs,"plazo"), rs.getString("codigo_forma_pago"),
                    rs.getString("codigo_tipo_cuota"), integer(rs,"amortizacion_capital"),
                    dec(rs,"valor_cuota"), date(rs,"primera_aparicion"), date(rs,"ultima_aparicion"),
                    bool(rs,"existe_corte_anterior"), bool(rs,"existe_corte_actual"),
                    dec(rs,"saldo_anterior"), dec(rs,"saldo_actual"), dec(rs,"variacion_saldo"),
                    rs.getString("tipo_movimiento"), integer(rs,"dias_anticipacion"),
                    rs.getString("rango_salida"), integer(rs,"dias_mora_anterior"),
                    integer(rs,"dias_mora_actual"), rs.getString("edad_contable_anterior"),
                    rs.getString("edad_contable_actual"),
                    dec(rs,"deterioro_capital_anterior"), dec(rs,"deterioro_capital_actual"),
                    dec(rs,"deterioro_intereses_anterior"), dec(rs,"deterioro_intereses_actual"),
                    dec(rs,"deterioro_otros_anterior"), dec(rs,"deterioro_otros_actual"),
                    dec(rs,"deterioro_total_anterior"), dec(rs,"deterioro_total_actual"),
                    dec(rs,"valor_nuevos"), dec(rs,"valor_reingresos"),
                    dec(rs,"valor_aumentos_saldo"), dec(rs,"valor_reducciones_saldo"),
                    dec(rs,"valor_prepagos"), dec(rs,"valor_cancelaciones_normales"),
                    dec(rs,"valor_cancelaciones_post_vencimiento"),
                    dec(rs,"valor_ausencias_temporales"), dec(rs,"valor_otras_salidas"),
                    dec(rs,"valor_movimiento_neto"));

    private static LocalDate date(ResultSet rs, String c) throws SQLException {
        var v = rs.getDate(c); return v == null ? null : v.toLocalDate();
    }
    private static BigDecimal dec(ResultSet rs, String c) throws SQLException {
        return rs.getBigDecimal(c);
    }
    private static Integer integer(ResultSet rs, String c) throws SQLException {
        Object v = rs.getObject(c); return v == null ? null : ((Number)v).intValue();
    }
    private static Long lng(ResultSet rs, String c) throws SQLException {
        Object v = rs.getObject(c); return v == null ? null : ((Number)v).longValue();
    }
    private static Boolean bool(ResultSet rs, String c) throws SQLException {
        Object v = rs.getObject(c); return v == null ? null : (Boolean)v;
    }
}
