package co.assip.erp.cartera.originacion.asociados;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OriginacionAsociadoRepository {

    private static final int LIMITE_RESULTADOS = 30;

    private static final BeanPropertyRowMapper<OriginacionAsociadoDTO>
            MAPPER =
            BeanPropertyRowMapper.newInstance(
                    OriginacionAsociadoDTO.class
            );

    private final NamedParameterJdbcTemplate jdbc;

    public OriginacionAsociadoRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    // =========================================================
    // BUSCAR
    // =========================================================

    public List<OriginacionAsociadoDTO> buscar(
            String documento,
            String nombres,
            String primerApellido,
            String segundoApellido,
            Integer idAgencia
    ) {

        String sql = """
                SELECT
                    v.id_datos_personal,
                    v.tipo_documento,
                    v.nombre_tipo_documento,
                    v.documento,
                    v.tipo_persona,
                    v.nombres,
                    v.primer_apellido,
                    v.segundo_apellido,

                    TRIM(
                        CONCAT_WS(
                            ' ',
                            NULLIF(TRIM(v.nombres), ''),
                            NULLIF(TRIM(v.primer_apellido), ''),
                            NULLIF(TRIM(v.segundo_apellido), '')
                        )
                    ) AS nombre_completo,

                    v.celular_uno,
                    v.telefono,
                    v.correo_personal,
                    v.ciudad_residencia,
                    v.fecha_apertura,
                    v.fecha_actualizacion,

                    CASE
                        WHEN v.fecha_actualizacion IS NULL THEN NULL
                        ELSE (
                            CURRENT_DATE - v.fecha_actualizacion
                        )::integer
                    END AS dias_sin_actualizacion,

                    parametro_actualizacion.valor_parametro::integer
                        AS dias_maximo_actualizacion,

                    CASE
                        WHEN v.fecha_actualizacion IS NULL THEN FALSE

                        WHEN parametro_actualizacion.valor_parametro IS NULL
                            THEN FALSE

                        WHEN (
                            CURRENT_DATE - v.fecha_actualizacion
                        )::integer <= parametro_actualizacion.valor_parametro
                            THEN TRUE

                        ELSE FALSE
                    END AS informacion_actualizada

                FROM reporting.vw_datos_personales_operativa v

                LEFT JOIN general.parametros parametro_actualizacion
                    ON parametro_actualizacion.id_agencia = :idAgencia
                   AND parametro_actualizacion.codigo_parametro = 121

                WHERE v.id_datos_personal IS NOT NULL

                  AND (
                        :documento = ''
                        OR UPPER(COALESCE(v.documento, ''))
                           LIKE UPPER(:documentoPatron)
                  )

                  AND (
                        :nombres = ''
                        OR UPPER(COALESCE(v.nombres, ''))
                           LIKE UPPER(:nombresPatron)
                  )

                  AND (
                        :primerApellido = ''
                        OR UPPER(COALESCE(v.primer_apellido, ''))
                           LIKE UPPER(:primerApellidoPatron)
                  )

                  AND (
                        :segundoApellido = ''
                        OR UPPER(COALESCE(v.segundo_apellido, ''))
                           LIKE UPPER(:segundoApellidoPatron)
                  )

                ORDER BY
                    v.primer_apellido,
                    v.segundo_apellido,
                    v.nombres,
                    v.documento

                LIMIT :limite
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "documento",
                                documento
                        )
                        .addValue(
                                "documentoPatron",
                                "%" + documento + "%"
                        )
                        .addValue(
                                "nombres",
                                nombres
                        )
                        .addValue(
                                "nombresPatron",
                                "%" + nombres + "%"
                        )
                        .addValue(
                                "primerApellido",
                                primerApellido
                        )
                        .addValue(
                                "primerApellidoPatron",
                                "%" + primerApellido + "%"
                        )
                        .addValue(
                                "segundoApellido",
                                segundoApellido
                        )
                        .addValue(
                                "segundoApellidoPatron",
                                "%" + segundoApellido + "%"
                        )
                        .addValue(
                                "idAgencia",
                                idAgencia
                        )
                        .addValue(
                                "limite",
                                LIMITE_RESULTADOS
                        );

        return jdbc.query(
                sql,
                params,
                MAPPER
        );
    }


    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    public OriginacionAsociadoDTO buscarPorId(
            Integer idDatosPersonal,
            Integer idAgencia
    ) {

        String sql = """
                SELECT
                    v.id_datos_personal,
                    v.tipo_documento,
                    v.nombre_tipo_documento,
                    v.documento,
                    v.tipo_persona,
                    v.nombres,
                    v.primer_apellido,
                    v.segundo_apellido,

                    TRIM(
                        CONCAT_WS(
                            ' ',
                            NULLIF(TRIM(v.nombres), ''),
                            NULLIF(TRIM(v.primer_apellido), ''),
                            NULLIF(TRIM(v.segundo_apellido), '')
                        )
                    ) AS nombre_completo,

                    v.celular_uno,
                    v.telefono,
                    v.correo_personal,
                    v.ciudad_residencia,
                    v.fecha_apertura,
                    v.fecha_actualizacion,

                    CASE
                        WHEN v.fecha_actualizacion IS NULL THEN NULL
                        ELSE (
                            CURRENT_DATE - v.fecha_actualizacion
                        )::integer
                    END AS dias_sin_actualizacion,

                    parametro_actualizacion.valor_parametro::integer
                        AS dias_maximo_actualizacion,

                    CASE
                        WHEN v.fecha_actualizacion IS NULL THEN FALSE

                        WHEN parametro_actualizacion.valor_parametro IS NULL
                            THEN FALSE

                        WHEN (
                            CURRENT_DATE - v.fecha_actualizacion
                        )::integer <= parametro_actualizacion.valor_parametro
                            THEN TRUE

                        ELSE FALSE
                    END AS informacion_actualizada

                FROM reporting.vw_datos_personales_operativa v

                LEFT JOIN general.parametros parametro_actualizacion
                    ON parametro_actualizacion.id_agencia = :idAgencia
                   AND parametro_actualizacion.codigo_parametro = 121

                WHERE v.id_datos_personal = :idDatosPersonal
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idDatosPersonal",
                                idDatosPersonal
                        )
                        .addValue(
                                "idAgencia",
                                idAgencia
                        );

        List<OriginacionAsociadoDTO> resultados =
                jdbc.query(
                        sql,
                        params,
                        MAPPER
                );

        return resultados.isEmpty()
                ? null
                : resultados.get(0);
    }
}