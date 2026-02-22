package co.assip.erp.nomina.periodos_nomina;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PeriodosGeneradorRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // EXISTEN PERÍODOS PARA AGENCIA + AÑO
    // =========================================================
    public boolean existenPeriodos(
            Integer idAgencia,
            Integer anio
    ) {

        String sql = """
            SELECT COUNT(1)
            FROM nomina.periodos_nomina
            WHERE id_agencia = :agencia
              AND anio = :anio
        """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("agencia", idAgencia)
                        .addValue("anio", anio);

        Integer count =
                jdbc.queryForObject(
                        sql,
                        params,
                        Integer.class
                );

        return count != null && count > 0;
    }

    // =========================================================
    // OBTENER CALENDARIO ACTIVO
    // =========================================================
    public Map<String, Object> obtenerCalendario(
            Integer idAgencia
    ) {

        String sql = """
            SELECT *
            FROM nomina.calendarios_nomina
            WHERE id_agencia = :agencia
              AND activo = TRUE
            LIMIT 1
        """;

        MapSqlParameterSource params =
                new MapSqlParameterSource("agencia", idAgencia);

        List<Map<String, Object>> rows =
                jdbc.queryForList(sql, params);

        return rows.isEmpty()
                ? null
                : rows.get(0);
    }

    // =========================================================
    // CREAR CALENDARIO DEFAULT
    // =========================================================
    public void crearCalendarioDefault(
            Integer idAgencia,
            String tipo,
            Integer idUsuario
    ) {

        String nombre =
                "Calendario " + tipo +
                        " - Agencia " + idAgencia;

        String sql = """
            INSERT INTO nomina.calendarios_nomina (
                id_agencia,
                nombre,
                tipo_periodo,
                activo,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            VALUES (
                :agencia,
                :nombre,
                :tipo,
                TRUE,
                :usuario,
                :usuario
            )
        """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("agencia", idAgencia)
                        .addValue("nombre", nombre)
                        .addValue("tipo", tipo)
                        .addValue(
                                "usuario",
                                idUsuario != null
                                        ? idUsuario
                                        : 1
                        );

        jdbc.update(sql, params);
    }

    // =========================================================
    // INSERTAR PERÍODO
    // =========================================================
    public void insertarPeriodo(
            MapSqlParameterSource params
    ) {

        String sql = """
            INSERT INTO nomina.periodos_nomina (
                id_agencia,
                id_calendario,
                anio,
                mes,
                tipo_periodo,
                numero_periodo,
                fecha_inicio,
                fecha_fin,
                descripcion,
                estado,
                fk_seguridad_creacion,
                fk_seguridad_edicion
            )
            VALUES (
                :idAgencia,
                :idCalendario,
                :anio,
                :mes,
                :tipo,
                :numero,
                :inicio,
                :fin,
                :descripcion,
                'ABIERTO',
                :usuario,
                :usuario
            )
        """;

        jdbc.update(sql, params);
    }
}
