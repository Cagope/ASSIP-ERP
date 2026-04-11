package co.assip.erp.nomina.periodos_nomina;

import co.assip.erp.nomina.periodos_nomina.dto.PeriodoNominaListDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PeriodosNominaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // LISTAR PERÍODOS
    // =========================================================
    public List<PeriodoNominaListDTO> listar(Integer idAgencia, Integer anio) {

        StringBuilder sql = new StringBuilder("""
            SELECT
              p.id_periodo,
              p.id_agencia,
              a.nombre_agencia,
              p.anio,
              p.mes,
              p.numero_periodo,
              p.tipo_periodo,
              p.descripcion,
              p.fecha_inicio,
              p.fecha_fin,
              p.estado,
              p.fecha_liquidacion,
              p.fk_seguridad_liquidacion,
              p.fecha_contabiliza,
              p.fk_seguridad_contabiliza
            FROM nomina.periodos_nomina p
            JOIN general.datos_agencias a
              ON a.id_agencia = p.id_agencia
            WHERE 1=1
        """);

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (idAgencia != null) {
            sql.append(" AND p.id_agencia = :idAgencia");
            params.addValue("idAgencia", idAgencia);
        }

        if (anio != null) {
            sql.append(" AND p.anio = :anio");
            params.addValue("anio", anio);
        }

        sql.append("""
            ORDER BY
              p.anio ASC,
              p.mes ASC,
              p.numero_periodo ASC,
              p.id_agencia ASC
        """);

        return jdbc.query(sql.toString(), params, (rs, rowNum) ->
                PeriodoNominaListDTO.builder()
                        .idPeriodo(rs.getInt("id_periodo"))
                        .idAgencia(rs.getInt("id_agencia"))
                        .nombreAgencia(rs.getString("nombre_agencia"))
                        .anio(rs.getInt("anio"))
                        .mes(rs.getInt("mes"))
                        .numeroPeriodo((Integer) rs.getObject("numero_periodo"))
                        .tipoPeriodo(rs.getString("tipo_periodo"))
                        .descripcion(rs.getString("descripcion"))
                        .fechaInicio(rs.getDate("fecha_inicio").toLocalDate())
                        .fechaFin(rs.getDate("fecha_fin").toLocalDate())
                        .estado(rs.getString("estado"))
                        .fechaLiquidacion(
                                rs.getTimestamp("fecha_liquidacion") != null
                                        ? rs.getTimestamp("fecha_liquidacion").toLocalDateTime()
                                        : null)
                        .fkSeguridadLiquidacion(
                                (Integer) rs.getObject("fk_seguridad_liquidacion"))
                        .fechaContabiliza(
                                rs.getTimestamp("fecha_contabiliza") != null
                                        ? rs.getTimestamp("fecha_contabiliza").toLocalDateTime()
                                        : null)
                        .fkSeguridadContabiliza(
                                (Integer) rs.getObject("fk_seguridad_contabiliza"))
                        .build()
        );
    }

    // =========================================================
    // CAMBIOS DE ESTADO
    // =========================================================
    public void cambiarEstado(Integer idPeriodo, String estado, Integer idUsuario) {

        String sql = """
            UPDATE nomina.periodos_nomina
            SET
              estado = :estado,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_periodo = :id
        """;

        jdbc.update(sql,
                new MapSqlParameterSource()
                        .addValue("id", idPeriodo)
                        .addValue("estado", estado)
                        .addValue("usr", idUsuario != null ? idUsuario : 1)
        );
    }

    public void marcarLiquidado(Integer idPeriodo, Integer idUsuario) {

        String sql = """
            UPDATE nomina.periodos_nomina
            SET
              fecha_liquidacion = CURRENT_TIMESTAMP,
              fk_seguridad_liquidacion = :usr,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_periodo = :id
        """;

        jdbc.update(sql,
                new MapSqlParameterSource()
                        .addValue("id", idPeriodo)
                        .addValue("usr", idUsuario != null ? idUsuario : 1)
        );
    }

    public void marcarCerrado(Integer idPeriodo, Integer idUsuario) {

        String sql = """
        UPDATE nomina.periodos_nomina
        SET
            estado = 'CERRADO',
            fk_seguridad_edicion = :idUsuario,
            fecha_edicion = NOW()
        WHERE id_periodo = :idPeriodo
          AND estado = 'ABIERTO'
    """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idPeriodo", idPeriodo)
                        .addValue("idUsuario", idUsuario)
        );
    }

    public void marcarContabilizado(Integer idPeriodo, Integer idUsuario) {

        String sql = """
            UPDATE nomina.periodos_nomina
            SET
              fecha_contabiliza = CURRENT_TIMESTAMP,
              fk_seguridad_contabiliza = :usr,
              fk_seguridad_edicion = :usr,
              fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_periodo = :id
        """;

        jdbc.update(sql,
                new MapSqlParameterSource()
                        .addValue("id", idPeriodo)
                        .addValue("usr", idUsuario != null ? idUsuario : 1)
        );
    }

    // =========================================================
    // 🔥 PERÍODO OPERATIVO ACTIVO
    // =========================================================
    public Integer obtenerPeriodoActivoId() {

        String sql = """
        SELECT p.id_periodo
        FROM nomina.periodos_nomina p
        WHERE UPPER(TRIM(p.estado)) = 'ABIERTO'
        ORDER BY
          p.fecha_inicio ASC,
          p.id_periodo ASC
        LIMIT 1
    """;

        return jdbc.queryForObject(sql, new MapSqlParameterSource(), Integer.class);
    }

    // =========================================================
    // 🔥 FECHAS DEL PERÍODO (USADO EN LIQUIDACIÓN / EXCEL)
    // =========================================================
    public PeriodoFechas obtenerFechas(Integer idPeriodo) {

        String sql = """
            SELECT
              p.fecha_inicio,
              p.fecha_fin
            FROM nomina.periodos_nomina p
            WHERE p.id_periodo = :id
        """;

        return jdbc.query(sql,
                new MapSqlParameterSource("id", idPeriodo),
                rs -> {
                    if (!rs.next()) return null;
                    return new PeriodoFechas(
                            rs.getObject("fecha_inicio", LocalDate.class),
                            rs.getObject("fecha_fin", LocalDate.class)
                    );
                }
        );
    }

    // =========================================================
    // DTO INTERNO
    // =========================================================
    public record PeriodoFechas(
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {}

    public record PeriodoActivoInfo(
            Integer idPeriodo,
            Integer idAgencia,
            String nombreAgencia,
            Integer anio,
            Integer mes,
            Integer numeroPeriodo,
            String descripcion,
            String estado
    ) {}

    // =========================================================
    // 🔒 VALIDACIÓN FUERTE: PERÍODO ABIERTO POR AGENCIA
    // =========================================================
    public void validarPeriodoAbierto(Integer idPeriodo, Integer idAgencia) {

        String sql = """
        SELECT COUNT(1)
        FROM nomina.periodos_nomina p
        WHERE p.id_periodo = :idPeriodo
          AND p.id_agencia = :idAgencia
          AND UPPER(TRIM(p.estado)) = 'ABIERTO'
    """;

        Integer count = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idPeriodo", idPeriodo)
                        .addValue("idAgencia", idAgencia),
                Integer.class
        );

        if (count == null || count == 0) {
            throw new IllegalStateException(
                    "El período no existe, no pertenece a la agencia o no está en estado ABIERTO"
            );
        }
    }

    // =========================================================
    // 🏷️ LABEL DEL PERÍODO (PARA LIQUIDACIÓN PREVIEW)
    // =========================================================
    public PeriodoLabel obtenerPeriodoLabel(Integer idPeriodo) {

        String sql = """
        SELECT
          p.id_periodo,
          p.anio,
          p.mes,
          p.numero_periodo,
          p.tipo_periodo
        FROM nomina.periodos_nomina p
        WHERE p.id_periodo = :id
    """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource("id", idPeriodo),
                rs -> {
                    if (!rs.next()) return null;

                    return new PeriodoLabel(
                            rs.getInt("id_periodo"),
                            rs.getInt("anio"),
                            rs.getInt("mes"),
                            (Integer) rs.getObject("numero_periodo"),
                            rs.getString("tipo_periodo")
                    );
                }
        );
    }

    // =========================================================
    // DTO INTERNO: LABEL DEL PERÍODO
    // =========================================================
    public record PeriodoLabel(
            Integer idPeriodo,
            Integer anio,
            Integer mes,
            Integer numeroPeriodo,
            String tipoPeriodo
    ) {}

    // =========================================================
    // 🔎 OBTENER AGENCIA DEL PERÍODO
    // =========================================================
    public Integer obtenerAgenciaDelPeriodo(Integer idPeriodo) {

        String sql = """
        SELECT id_agencia
        FROM nomina.periodos_nomina
        WHERE id_periodo = :idPeriodo
    """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource("idPeriodo", idPeriodo),
                Integer.class
        );
    }

    // =========================================================
    // 🔎 PERÍODOS DISPONIBLES PARA CONTABILIZACIÓN
    // =========================================================
    public List<PeriodoNominaListDTO> listarParaContabilizacion() {

        String sql = """
        SELECT
          p.id_periodo,
          p.id_agencia,
          a.nombre_agencia,
          p.anio,
          p.mes,
          p.numero_periodo,
          p.tipo_periodo,
          p.descripcion,
          p.fecha_inicio,
          p.fecha_fin,
          p.estado,
          p.fecha_liquidacion,
          p.fk_seguridad_liquidacion,
          p.fecha_contabiliza,
          p.fk_seguridad_contabiliza
        FROM nomina.periodos_nomina p
        JOIN general.datos_agencias a
          ON a.id_agencia = p.id_agencia
        WHERE
            UPPER(TRIM(p.estado)) IN ('CERRADO','CONTABILIZADO')
        ORDER BY
          a.nombre_agencia,
          p.anio ASC,
          p.mes ASC,
          p.numero_periodo ASC
    """;

        return jdbc.query(sql, new MapSqlParameterSource(), (rs, rowNum) ->
                PeriodoNominaListDTO.builder()
                        .idPeriodo(rs.getInt("id_periodo"))
                        .idAgencia(rs.getInt("id_agencia"))
                        .nombreAgencia(rs.getString("nombre_agencia"))
                        .anio(rs.getInt("anio"))
                        .mes(rs.getInt("mes"))
                        .numeroPeriodo((Integer) rs.getObject("numero_periodo"))
                        .tipoPeriodo(rs.getString("tipo_periodo"))
                        .descripcion(rs.getString("descripcion"))
                        .fechaInicio(rs.getDate("fecha_inicio").toLocalDate())
                        .fechaFin(rs.getDate("fecha_fin").toLocalDate())
                        .estado(rs.getString("estado"))
                        .fechaLiquidacion(
                                rs.getTimestamp("fecha_liquidacion") != null
                                        ? rs.getTimestamp("fecha_liquidacion").toLocalDateTime()
                                        : null)
                        .fkSeguridadLiquidacion(
                                (Integer) rs.getObject("fk_seguridad_liquidacion"))
                        .fechaContabiliza(
                                rs.getTimestamp("fecha_contabiliza") != null
                                        ? rs.getTimestamp("fecha_contabiliza").toLocalDateTime()
                                        : null)
                        .fkSeguridadContabiliza(
                                (Integer) rs.getObject("fk_seguridad_contabiliza"))
                        .build()
        );
    }

    // =========================================================
// 🔎 DETALLE COMPLETO DEL PERÍODO ACTIVO
// =========================================================
    public PeriodoActivoInfo obtenerPeriodoActivoInfo() {

        String sql = """
        SELECT
          p.id_periodo,
          p.id_agencia,
          a.nombre_agencia,
          p.anio,
          p.mes,
          p.numero_periodo,
          p.descripcion,
          p.estado
        FROM nomina.periodos_nomina p
        JOIN general.datos_agencias a
          ON a.id_agencia = p.id_agencia
        WHERE UPPER(TRIM(p.estado)) = 'ABIERTO'
        ORDER BY
          p.fecha_inicio ASC,
          p.id_periodo ASC
        LIMIT 1
    """;

        return jdbc.query(sql, new MapSqlParameterSource(), rs -> {
            if (!rs.next()) return null;

            return new PeriodoActivoInfo(
                    rs.getInt("id_periodo"),
                    rs.getInt("id_agencia"),
                    rs.getString("nombre_agencia"),
                    rs.getInt("anio"),
                    rs.getInt("mes"),
                    (Integer) rs.getObject("numero_periodo"),
                    rs.getString("descripcion"),
                    rs.getString("estado")
            );
        });
    }

}