package co.assip.erp.cartera.buscadorasociados;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Types;

import java.util.List;

/**
 * Repositorio transversal para buscar asociados desde
 * los diferentes procesos del módulo de Cartera.
 *
 * Características:
 *
 * - Solo lectura.
 * - Busca únicamente personas con créditos registrados.
 * - Incluye personas con créditos vigentes, saldados o cancelados.
 * - No filtra por saldo.
 * - No depende del módulo de Depósitos.
 * - Devuelve máximo 100 asociados.
 */
@Repository
public class CarteraAsociadoBusquedaRepository {

    // =========================================================
    // Parámetros
    // =========================================================

    private static final String PARAM_DOCUMENTO =
            "documento";

    private static final String PARAM_NOMBRES =
            "nombres";

    private static final String PARAM_PRIMER_APELLIDO =
            "primerApellido";

    private static final String PARAM_SEGUNDO_APELLIDO =
            "segundoApellido";

    // =========================================================
    // SQL
    // =========================================================

    private static final String SQL_BUSCAR_ASOCIADOS = """
            SELECT
                p.id_datos_personal,

                p.tipo_documento,
                p.nombre_tipo_documento,
                p.documento,

                p.nombres,
                p.primer_apellido,
                p.segundo_apellido,

                CONCAT_WS(
                    ' ',
                    NULLIF(TRIM(p.nombres), ''),
                    NULLIF(TRIM(p.primer_apellido), ''),
                    NULLIF(TRIM(p.segundo_apellido), '')
                ) AS nombre_completo,

                CAST(
                    (
                        SELECT COUNT(*)
                        FROM cartera.vw_cartera_creditos_total c_count
                        WHERE c_count.id_datos_personal =
                              p.id_datos_personal
                    )
                    AS integer
                ) AS cantidad_creditos

            FROM reporting.vw_datos_personales_operativa p

            WHERE
                EXISTS (
                    SELECT 1
                    FROM cartera.vw_cartera_creditos_total c
                    WHERE c.id_datos_personal =
                          p.id_datos_personal
                )

                AND (
                    :documento IS NULL
                    OR TRIM(p.documento::text)
                       ILIKE '%' || :documento || '%'
                )

                AND (
                    :nombres IS NULL
                    OR COALESCE(
                        TRIM(p.nombres),
                        ''
                    ) ILIKE '%' || :nombres || '%'
                )

                AND (
                    :primerApellido IS NULL
                    OR COALESCE(
                        TRIM(p.primer_apellido),
                        ''
                    ) ILIKE '%' || :primerApellido || '%'
                )

                AND (
                    :segundoApellido IS NULL
                    OR COALESCE(
                        TRIM(p.segundo_apellido),
                        ''
                    ) ILIKE '%' || :segundoApellido || '%'
                )

            ORDER BY
                COALESCE(
                    TRIM(p.primer_apellido),
                    ''
                ),
                COALESCE(
                    TRIM(p.segundo_apellido),
                    ''
                ),
                COALESCE(
                    TRIM(p.nombres),
                    ''
                ),
                p.documento

            LIMIT 100
            """;

    // =========================================================
    // Dependencia
    // =========================================================

    private final NamedParameterJdbcTemplate jdbc;

    public CarteraAsociadoBusquedaRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // Mapeador
    // =========================================================

    private static final BeanPropertyRowMapper<CarteraAsociadoBusquedaDTO>
            ASOCIADO_MAPPER =
            crearMapper();

    private static BeanPropertyRowMapper<CarteraAsociadoBusquedaDTO>
    crearMapper() {

        BeanPropertyRowMapper<CarteraAsociadoBusquedaDTO> mapper =
                BeanPropertyRowMapper.newInstance(
                        CarteraAsociadoBusquedaDTO.class
                );

        /*
         * La vista puede contener columnas adicionales que no
         * forman parte del DTO de selección.
         */
        mapper.setCheckFullyPopulated(false);

        /*
         * Permite mapear valores nulos sin forzar valores
         * primitivos.
         */
        mapper.setPrimitivesDefaultedForNullValue(true);

        return mapper;
    }

    // =========================================================
    // Búsqueda
    // =========================================================

    public List<CarteraAsociadoBusquedaDTO> buscar(
            String documento,
            String nombres,
            String primerApellido,
            String segundoApellido
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                PARAM_DOCUMENTO,
                                normalizarParametro(
                                        documento
                                ),
                                Types.VARCHAR
                        )
                        .addValue(
                                PARAM_NOMBRES,
                                normalizarParametro(
                                        nombres
                                ),
                                Types.VARCHAR
                        )
                        .addValue(
                                PARAM_PRIMER_APELLIDO,
                                normalizarParametro(
                                        primerApellido
                                ),
                                Types.VARCHAR
                        )
                        .addValue(
                                PARAM_SEGUNDO_APELLIDO,
                                normalizarParametro(
                                        segundoApellido
                                ),
                                Types.VARCHAR
                        );

        return jdbc.query(
                SQL_BUSCAR_ASOCIADOS,
                parametros,
                ASOCIADO_MAPPER
        );
    }

    // =========================================================
    // Utilidades
    // =========================================================

    private String normalizarParametro(
            String valor
    ) {

        if (valor == null) {
            return null;
        }

        String normalizado =
                valor
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );

        return normalizado.isBlank()
                ? null
                : normalizado;
    }

}