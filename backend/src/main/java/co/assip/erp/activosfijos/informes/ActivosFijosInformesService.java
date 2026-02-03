package co.assip.erp.activosfijos.informes;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivosFijosInformesService {

    private final NamedParameterJdbcTemplate jdbc;

    // ============================================================
    // ✅ INFORME MAESTRO DE ACTIVOS FIJOS
    // Fuente: activos_fijos.vw_activos_fijos_maestro_total
    // GET /activos-fijos/informes/maestro-activos
    // ============================================================
    public List<Map<String, Object>> obtenerMaestroActivos() {

        String sql = """
            SELECT *
            FROM activos_fijos.vw_activos_fijos_maestro_total
            ORDER BY id_activo_fijo DESC
        """;

        return jdbc.queryForList(sql, Map.of());
    }

    // ============================================================
    // ✅ MOVIMIENTOS / KARDEX GENERAL DE ACTIVOS FIJOS
    // Fuente: activos_fijos.vw_activos_movimientos_total
    // GET /activos-fijos/informes/movimientos-activos
    //
    // Filtros opcionales:
    // - idAgencia
    // - codigoMovimiento
    // - fechaIni (yyyy-MM-dd)
    // - fechaFin (yyyy-MM-dd)
    // - idActivoFijo
    // ============================================================
    public List<Map<String, Object>> obtenerMovimientosActivos(
            Integer idAgencia,
            String codigoMovimiento,
            String fechaIni,
            String fechaFin,
            Long idActivoFijo
    ) {

        StringBuilder sql = new StringBuilder("""
            SELECT *
            FROM activos_fijos.vw_activos_movimientos_total
            WHERE 1=1
        """);

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (idAgencia != null) {
            sql.append(" AND id_agencia = :idAgencia ");
            params.addValue("idAgencia", idAgencia);
        }

        if (idActivoFijo != null) {
            sql.append(" AND id_activo_fijo = :idActivoFijo ");
            params.addValue("idActivoFijo", idActivoFijo);
        }

        if (codigoMovimiento != null && !codigoMovimiento.isBlank()) {
            sql.append(" AND codigo_movimiento = :codigoMovimiento ");
            params.addValue("codigoMovimiento", codigoMovimiento);
        }

        if (fechaIni != null && !fechaIni.isBlank()) {
            sql.append(" AND fecha >= :fechaIni::date ");
            params.addValue("fechaIni", fechaIni);
        }

        if (fechaFin != null && !fechaFin.isBlank()) {
            sql.append(" AND fecha <= :fechaFin::date ");
            params.addValue("fechaFin", fechaFin);
        }

        sql.append(" ORDER BY fecha DESC, hora DESC, id_extracto DESC ");

        return jdbc.queryForList(sql.toString(), params);
    }
    public List<Map<String, Object>> obtenerResumenMovimientos(
            Integer idAgencia,
            String codigoMovimiento
    ) {

        StringBuilder sql = new StringBuilder("""
        SELECT *
        FROM activos_fijos.vw_activos_movimientos_resumen
        WHERE 1=1
    """);

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (idAgencia != null) {
            sql.append(" AND id_agencia = :idAgencia ");
            params.addValue("idAgencia", idAgencia);
        }

        if (codigoMovimiento != null && !codigoMovimiento.isBlank()) {
            sql.append(" AND codigo_movimiento = :codigoMovimiento ");
            params.addValue("codigoMovimiento", codigoMovimiento);
        }

        sql.append(" ORDER BY periodo_mes DESC, codigo_movimiento, id_agencia ");

        return jdbc.queryForList(sql.toString(), params);
    }

    public List<Map<String, Object>> obtenerActivosMovimientos() {

        String sql = """
        SELECT
          codigo_movimiento,
          nombre,
          contable,
          procesa
        FROM activos_fijos.tipos_movimientos_activos
        ORDER BY codigo_movimiento
    """;

        return jdbc.queryForList(sql, Map.of());
    }


}
