package co.assip.erp.cartera.analisis.cosechas;

import co.assip.erp.cartera.analisis.cosechas.dto.CosechaCatalogoDTO;
import co.assip.erp.cartera.analisis.cosechas.dto.CosechaCeldaDTO;
import co.assip.erp.cartera.analisis.cosechas.dto.CosechaCorteDTO;
import co.assip.erp.cartera.analisis.cosechas.dto.CosechaDetalleDTO;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class AnalisisCosechasRepository {

    private final NamedParameterJdbcTemplate jdbc;


    public AnalisisCosechasRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    // =========================================================
    // CORTES
    // =========================================================

    public List<CosechaCorteDTO> listarCortes() {

        String sql = """
                SELECT
                    h.fecha_corte,

                    COUNT(
                        DISTINCT h.id_cartera_credito
                    )::integer AS cantidad_creditos

                FROM cartera.vw_cartera_resultados_mensuales_total h

                WHERE h.fecha_corte IS NOT NULL

                GROUP BY
                    h.fecha_corte

                ORDER BY
                    h.fecha_corte DESC
                """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                new BeanPropertyRowMapper<>(
                        CosechaCorteDTO.class
                )
        );
    }


    public LocalDate obtenerUltimoCorte() {

        String sql = """
                SELECT
                    MAX(h.fecha_corte)
                FROM cartera.vw_cartera_resultados_mensuales_total h
                """;

        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource(),
                LocalDate.class
        );
    }


    public boolean existeCorte(
            LocalDate fechaCorte
    ) {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM cartera.vw_cartera_resultados_mensuales_total h
                    WHERE h.fecha_corte = :fechaCorte
                )
                """;

        Boolean existe =
                jdbc.queryForObject(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "fechaCorte",
                                        fechaCorte
                                ),
                        Boolean.class
                );

        return Boolean.TRUE.equals(
                existe
        );
    }


    // =========================================================
    // AGENCIAS
    // =========================================================

    public List<CosechaCatalogoDTO> listarAgencias() {

        String sql = """
                SELECT
                    a.id_agencia AS id,
                    a.codigo_agencia::text AS codigo,
                    a.nombre_agencia AS nombre

                FROM general.datos_agencias a

                ORDER BY
                    a.nombre_agencia
                """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                new BeanPropertyRowMapper<>(
                        CosechaCatalogoDTO.class
                )
        );
    }


    // =========================================================
    // LÍNEAS
    // =========================================================

    public List<CosechaCatalogoDTO> listarLineas() {

        String sql = """
                SELECT DISTINCT
                    h.id_linea_credito AS id,
                    h.codigo_linea_credito AS codigo,
                    h.nombre_linea_credito AS nombre

                FROM cartera.vw_cartera_resultados_mensuales_total h

                WHERE h.id_linea_credito IS NOT NULL

                ORDER BY
                    codigo,
                    nombre,
                    id
                """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                new BeanPropertyRowMapper<>(
                        CosechaCatalogoDTO.class
                )
        );
    }


    // =========================================================
    // ANÁLISIS DE COSECHAS
    // =========================================================

    public List<CosechaCeldaDTO> analizar(
            LocalDate cosechaDesde,
            LocalDate cosechaHasta,
            LocalDate hastaCorte,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        String sql = """
            WITH poblacion AS MATERIALIZED (

                SELECT DISTINCT ON (
                    h.id_cartera_credito
                )

                    h.id_cartera_credito,
                    h.id_agencia,
                    h.id_linea_credito,

                    h.fecha_desembolso,

                    date_trunc(
                        'month',
                        h.fecha_desembolso
                    )::date AS cosecha,

                    COALESCE(
                        h.valor_inicial_credito,
                        0
                    ) AS valor_inicial_credito,

                    COALESCE(
                        h.valor_desembolsado,
                        h.valor_inicial_credito,
                        0
                    ) AS valor_desembolsado

                FROM cartera.vw_cartera_resultados_mensuales_total h

                WHERE h.fecha_desembolso IS NOT NULL

                  AND h.fecha_desembolso >=
                      :cosechaDesde

                  AND h.fecha_desembolso <
                      (
                          :cosechaHasta::date
                          + INTERVAL '1 month'
                      )

                  AND (
                       CAST(:idAgencia AS integer) IS NULL
                       OR h.id_agencia = CAST(:idAgencia AS integer)
                   )

                  AND (
                       CAST(:idLineaCredito AS integer) IS NULL
                       OR h.id_linea_credito = CAST(:idLineaCredito AS integer)
                   )

                ORDER BY
                    h.id_cartera_credito,
                    h.fecha_corte ASC,
                    h.id_cierre_cartera DESC,
                    h.id_cierre_cartera_credito DESC
            ),

            originacion AS MATERIALIZED (

                SELECT
                    p.cosecha,

                    COUNT(*)::integer
                        AS cantidad_originada,

                    COALESCE(
                        SUM(
                            p.valor_inicial_credito
                        ),
                        0
                    ) AS valor_inicial_original,

                    COALESCE(
                        SUM(
                            p.valor_desembolsado
                        ),
                        0
                    ) AS valor_desembolsado_original

                FROM poblacion p

                GROUP BY
                    p.cosecha
            ),

            cortes AS MATERIALIZED (

                SELECT DISTINCT
                    h.fecha_corte

                FROM cartera.vw_cartera_resultados_mensuales_total h

                WHERE h.fecha_corte <= :hastaCorte
            ),

            historia AS MATERIALIZED (

                SELECT DISTINCT ON (
                    h.fecha_corte,
                    h.id_cartera_credito
                )

                    h.fecha_corte,
                    h.id_cartera_credito,

                    COALESCE(
                        h.saldo_credito_fecha_corte,
                        0
                    ) AS saldo_capital,

                    COALESCE(
                        h.dias_mora,
                        0
                    )::integer AS dias_mora

                FROM cartera.vw_cartera_resultados_mensuales_total h

                INNER JOIN poblacion p
                        ON p.id_cartera_credito =
                           h.id_cartera_credito

                WHERE h.fecha_corte <= :hastaCorte

                ORDER BY
                    h.fecha_corte,
                    h.id_cartera_credito,
                    h.id_cierre_cartera DESC,
                    h.id_cierre_cartera_credito DESC
            ),

            observacion AS MATERIALIZED (

                SELECT
                    p.cosecha,
                    h.fecha_corte,

                    (
                        (
                            EXTRACT(
                                YEAR FROM h.fecha_corte
                            )::integer * 12
                        )
                        +
                        EXTRACT(
                            MONTH FROM h.fecha_corte
                        )::integer
                    )
                    -
                    (
                        (
                            EXTRACT(
                                YEAR FROM p.cosecha
                            )::integer * 12
                        )
                        +
                        EXTRACT(
                            MONTH FROM p.cosecha
                        )::integer
                    ) AS mob,

                    COUNT(
                        h.id_cartera_credito
                    )::integer
                        AS cantidad_presentes_corte,

                    COUNT(
                        h.id_cartera_credito
                    ) FILTER (
                        WHERE h.saldo_capital > 0
                    )::integer
                        AS cantidad_con_saldo,

                    COALESCE(
                        SUM(
                            h.saldo_capital
                        ),
                        0
                    ) AS saldo_capital,


                    COUNT(
                        h.id_cartera_credito
                    ) FILTER (
                        WHERE h.saldo_capital > 0
                          AND h.dias_mora >= 30
                    )::integer
                        AS cantidad_mora_30,

                    COALESCE(
                        SUM(
                            h.saldo_capital
                        ) FILTER (
                            WHERE h.saldo_capital > 0
                              AND h.dias_mora >= 30
                        ),
                        0
                    ) AS saldo_mora_30,


                    COUNT(
                        h.id_cartera_credito
                    ) FILTER (
                        WHERE h.saldo_capital > 0
                          AND h.dias_mora >= 60
                    )::integer
                        AS cantidad_mora_60,

                    COALESCE(
                        SUM(
                            h.saldo_capital
                        ) FILTER (
                            WHERE h.saldo_capital > 0
                              AND h.dias_mora >= 60
                        ),
                        0
                    ) AS saldo_mora_60,


                    COUNT(
                        h.id_cartera_credito
                    ) FILTER (
                        WHERE h.saldo_capital > 0
                          AND h.dias_mora >= 90
                    )::integer
                        AS cantidad_mora_90,

                    COALESCE(
                        SUM(
                            h.saldo_capital
                        ) FILTER (
                            WHERE h.saldo_capital > 0
                              AND h.dias_mora >= 90
                        ),
                        0
                    ) AS saldo_mora_90,


                    COUNT(
                        h.id_cartera_credito
                    ) FILTER (
                        WHERE h.saldo_capital > 0
                          AND h.dias_mora >= 180
                    )::integer
                        AS cantidad_mora_180,

                    COALESCE(
                        SUM(
                            h.saldo_capital
                        ) FILTER (
                            WHERE h.saldo_capital > 0
                              AND h.dias_mora >= 180
                        ),
                        0
                    ) AS saldo_mora_180

                FROM poblacion p

                INNER JOIN historia h
                        ON h.id_cartera_credito =
                           p.id_cartera_credito

                WHERE h.fecha_corte >=
                      (
                          date_trunc(
                              'month',
                              p.cosecha
                          )
                          +
                          INTERVAL '1 month'
                          -
                          INTERVAL '1 day'
                      )::date

                GROUP BY
                    p.cosecha,
                    h.fecha_corte
            ),

            malla AS (

                SELECT
                    o.cosecha,
                    c.fecha_corte,

                    (
                        (
                            EXTRACT(
                                YEAR FROM c.fecha_corte
                            )::integer * 12
                        )
                        +
                        EXTRACT(
                            MONTH FROM c.fecha_corte
                        )::integer
                    )
                    -
                    (
                        (
                            EXTRACT(
                                YEAR FROM o.cosecha
                            )::integer * 12
                        )
                        +
                        EXTRACT(
                            MONTH FROM o.cosecha
                        )::integer
                    ) AS mob,

                    o.cantidad_originada,
                    o.valor_inicial_original,
                    o.valor_desembolsado_original

                FROM originacion o

                CROSS JOIN cortes c

                WHERE c.fecha_corte >=
                      (
                          date_trunc(
                              'month',
                              o.cosecha
                          )
                          +
                          INTERVAL '1 month'
                          -
                          INTERVAL '1 day'
                      )::date

                  AND c.fecha_corte <=
                      :hastaCorte
            )

            SELECT
                m.cosecha,
                m.fecha_corte,
                m.mob,

                m.cantidad_originada,

                m.valor_inicial_original,

                m.valor_desembolsado_original,


                COALESCE(
                    obs.cantidad_presentes_corte,
                    0
                ) AS cantidad_presentes_corte,

                (
                    m.cantidad_originada
                    -
                    COALESCE(
                        obs.cantidad_presentes_corte,
                        0
                    )
                ) AS cantidad_sin_presencia_corte,

                CASE
                    WHEN m.cantidad_originada = 0
                        THEN 0
                    ELSE ROUND(
                        (
                            COALESCE(
                                obs.cantidad_presentes_corte,
                                0
                            )::numeric
                            /
                            m.cantidad_originada
                        ) * 100,
                        2
                    )
                END AS porcentaje_presentes_corte,

                CASE
                    WHEN m.cantidad_originada = 0
                        THEN 0
                    ELSE ROUND(
                        (
                            (
                                m.cantidad_originada
                                -
                                COALESCE(
                                    obs.cantidad_presentes_corte,
                                    0
                                )
                            )::numeric
                            /
                            m.cantidad_originada
                        ) * 100,
                        2
                    )
                END AS porcentaje_sin_presencia_corte,


                COALESCE(
                    obs.cantidad_con_saldo,
                    0
                ) AS cantidad_con_saldo,

                COALESCE(
                    obs.saldo_capital,
                    0
                ) AS saldo_capital,

                CASE
                    WHEN m.valor_desembolsado_original = 0
                        THEN 0
                    ELSE ROUND(
                        (
                            COALESCE(
                                obs.saldo_capital,
                                0
                            )
                            /
                            m.valor_desembolsado_original
                        ) * 100,
                        2
                    )
                END AS porcentaje_saldo_remanente,


                COALESCE(
                    obs.cantidad_mora_30,
                    0
                ) AS cantidad_mora30,

                COALESCE(
                    obs.saldo_mora_30,
                    0
                ) AS saldo_mora30,

                CASE
                    WHEN m.cantidad_originada = 0
                        THEN 0
                    ELSE ROUND(
                        (
                            COALESCE(
                                obs.cantidad_mora_30,
                                0
                            )::numeric
                            /
                            m.cantidad_originada
                        ) * 100,
                        2
                    )
                END AS porcentaje_cantidad_mora30,

                CASE
                    WHEN COALESCE(
                             obs.saldo_capital,
                             0
                         ) = 0
                        THEN 0
                    ELSE ROUND(
                        (
                            COALESCE(
                                obs.saldo_mora_30,
                                0
                            )
                            /
                            obs.saldo_capital
                        ) * 100,
                        2
                    )
                END AS porcentaje_saldo_mora30_sobre_saldo,

                CASE
                    WHEN m.valor_desembolsado_original = 0
                        THEN 0
                    ELSE ROUND(
                        (
                            COALESCE(
                                obs.saldo_mora_30,
                                0
                            )
                            /
                            m.valor_desembolsado_original
                        ) * 100,
                        2
                    )
                END AS porcentaje_saldo_mora30_sobre_originacion,


                COALESCE(
                    obs.cantidad_mora_60,
                    0
                ) AS cantidad_mora60,

                COALESCE(
                    obs.saldo_mora_60,
                    0
                ) AS saldo_mora60,

                CASE
                    WHEN m.cantidad_originada = 0
                        THEN 0
                    ELSE ROUND(
                        (
                            COALESCE(
                                obs.cantidad_mora_60,
                                0
                            )::numeric
                            /
                            m.cantidad_originada
                        ) * 100,
                        2
                    )
                END AS porcentaje_cantidad_mora60,

                CASE
                    WHEN COALESCE(
                             obs.saldo_capital,
                             0
                         ) = 0
                        THEN 0
                    ELSE ROUND(
                        (
                            COALESCE(
                                obs.saldo_mora_60,
                                0
                            )
                            /
                            obs.saldo_capital
                        ) * 100,
                        2
                    )
                END AS porcentaje_saldo_mora60_sobre_saldo,

                CASE
                    WHEN m.valor_desembolsado_original = 0
                        THEN 0
                    ELSE ROUND(
                        (
                            COALESCE(
                                obs.saldo_mora_60,
                                0
                            )
                            /
                            m.valor_desembolsado_original
                        ) * 100,
                        2
                    )
                END AS porcentaje_saldo_mora60_sobre_originacion,


                COALESCE(
                    obs.cantidad_mora_90,
                    0
                ) AS cantidad_mora90,

                COALESCE(
                    obs.saldo_mora_90,
                    0
                ) AS saldo_mora90,

                CASE
                    WHEN m.cantidad_originada = 0
                        THEN 0
                    ELSE ROUND(
                        (
                            COALESCE(
                                obs.cantidad_mora_90,
                                0
                            )::numeric
                            /
                            m.cantidad_originada
                        ) * 100,
                        2
                    )
                END AS porcentaje_cantidad_mora90,

                CASE
                    WHEN COALESCE(
                             obs.saldo_capital,
                             0
                         ) = 0
                        THEN 0
                    ELSE ROUND(
                        (
                            COALESCE(
                                obs.saldo_mora_90,
                                0
                            )
                            /
                            obs.saldo_capital
                        ) * 100,
                        2
                    )
                END AS porcentaje_saldo_mora90_sobre_saldo,

                CASE
                    WHEN m.valor_desembolsado_original = 0
                        THEN 0
                    ELSE ROUND(
                        (
                            COALESCE(
                                obs.saldo_mora_90,
                                0
                            )
                            /
                            m.valor_desembolsado_original
                        ) * 100,
                        2
                    )
                END AS porcentaje_saldo_mora90_sobre_originacion,


                COALESCE(
                    obs.cantidad_mora_180,
                    0
                ) AS cantidad_mora180,

                COALESCE(
                    obs.saldo_mora_180,
                    0
                ) AS saldo_mora180,

                CASE
                    WHEN m.cantidad_originada = 0
                        THEN 0
                    ELSE ROUND(
                        (
                            COALESCE(
                                obs.cantidad_mora_180,
                                0
                            )::numeric
                            /
                            m.cantidad_originada
                        ) * 100,
                        2
                    )
                END AS porcentaje_cantidad_mora180,

                CASE
                    WHEN COALESCE(
                             obs.saldo_capital,
                             0
                         ) = 0
                        THEN 0
                    ELSE ROUND(
                        (
                            COALESCE(
                                obs.saldo_mora_180,
                                0
                            )
                            /
                            obs.saldo_capital
                        ) * 100,
                        2
                    )
                END AS porcentaje_saldo_mora180_sobre_saldo,

                CASE
                    WHEN m.valor_desembolsado_original = 0
                        THEN 0
                    ELSE ROUND(
                        (
                            COALESCE(
                                obs.saldo_mora_180,
                                0
                            )
                            /
                            m.valor_desembolsado_original
                        ) * 100,
                        2
                    )
                END AS porcentaje_saldo_mora180_sobre_originacion

            FROM malla m

            LEFT JOIN observacion obs
                   ON obs.cosecha =
                      m.cosecha
                  AND obs.fecha_corte =
                      m.fecha_corte

            WHERE m.mob >= 0

            ORDER BY
                m.cosecha,
                m.fecha_corte
            """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "cosechaDesde",
                                cosechaDesde
                        )
                        .addValue(
                                "cosechaHasta",
                                cosechaHasta
                        )
                        .addValue(
                                "hastaCorte",
                                hastaCorte
                        )
                        .addValue(
                                "idAgencia",
                                idAgencia
                        )
                        .addValue(
                                "idLineaCredito",
                                idLineaCredito
                        );

        return jdbc.query(
                sql,
                params,
                new BeanPropertyRowMapper<>(
                        CosechaCeldaDTO.class
                )
        );
    }


    // =========================================================
    // DETALLE / INFORME DETALLADO
    // =========================================================

    public List<CosechaDetalleDTO> listarDetalle(
            LocalDate cosecha,
            LocalDate fechaCorte,
            String indicador,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        String sql = """
                WITH poblacion AS MATERIALIZED (

                    SELECT DISTINCT ON (
                        h.id_cartera_credito
                    )

                        h.id_cartera_credito,
                        h.id_agencia,
                        h.id_linea_credito,
                        h.codigo_linea_credito,
                        h.nombre_linea_credito,
                        h.pagare_cartera,

                        h.id_datos_personal,
                        h.tipo_documento,
                        h.documento,
                        h.nombre_completo,

                        h.fecha_desembolso,

                        date_trunc(
                            'month',
                            h.fecha_desembolso
                        )::date AS cosecha,

                        COALESCE(
                            h.valor_inicial_credito,
                            0
                        ) AS valor_inicial_credito,

                        COALESCE(
                            h.valor_desembolsado,
                            h.valor_inicial_credito,
                            0
                        ) AS valor_desembolsado

                    FROM cartera.vw_cartera_resultados_mensuales_total h

                    WHERE h.fecha_desembolso >= :cosecha

                      AND h.fecha_desembolso <
                          (
                              :cosecha::date
                              + INTERVAL '1 month'
                          )

                      AND (
                           CAST(:idAgencia AS integer) IS NULL
                           OR h.id_agencia = CAST(:idAgencia AS integer)
                       )
                
                       AND (
                           CAST(:idLineaCredito AS integer) IS NULL
                           OR h.id_linea_credito = CAST(:idLineaCredito AS integer)
                       )

                    ORDER BY
                        h.id_cartera_credito,
                        h.fecha_corte ASC,
                        h.id_cierre_cartera DESC,
                        h.id_cierre_cartera_credito DESC
                ),

                observacion AS MATERIALIZED (

                    SELECT DISTINCT ON (
                        h.id_cartera_credito
                    )

                        h.id_cartera_credito,

                        h.fecha_corte,

                        COALESCE(
                            h.saldo_credito_fecha_corte,
                            0
                        ) AS saldo_capital,

                        COALESCE(
                            h.dias_mora,
                            0
                        )::integer AS dias_mora,

                        h.codigo_estado_cartera,

                        h.descripcion_estado_cartera,

                        COALESCE(
                            h.edad_riesgo_inicial_resultado,
                            h.edad_riesgo_inicial_fotografia
                        ) AS edad_riesgo_inicial,

                        COALESCE(
                            h.edad_de_mora_resultado,
                            h.edad_de_mora_fotografia
                        ) AS edad_mora,

                        COALESCE(
                            h.edad_de_riesgo_resultado,
                            h.edad_de_riesgo_fotografia
                        ) AS edad_riesgo,

                        COALESCE(
                            h.edad_de_pe_resultado,
                            h.edad_de_pe_fotografia
                        ) AS edad_pe,

                        COALESCE(
                            h.edad_de_homologacion_resultado,
                            h.edad_de_homologacion_fotografia
                        ) AS edad_homologacion,

                        COALESCE(
                            h.edad_contable_resultado,
                            h.edad_contable_fotografia
                        ) AS edad_contable,

                        COALESCE(
                            h.vea,
                            0
                        ) AS vea,

                        COALESCE(
                            h.pi,
                            0
                        ) AS pi,

                        COALESCE(
                            h.pdi,
                            0
                        ) AS pdi,

                        COALESCE(
                            h.perdida_esperada,
                            0
                        ) AS perdida_esperada,

                        COALESCE(
                            h.deterioro_capital,
                            0
                        ) AS deterioro_capital,

                        COALESCE(
                            h.deterioro_intereses,
                            0
                        ) AS deterioro_intereses,

                        COALESCE(
                            h.deterioro_otros,
                            0
                        ) AS deterioro_otros,

                        COALESCE(
                            h.deterioro_total,
                            0
                        ) AS deterioro_total

                    FROM cartera.vw_cartera_resultados_mensuales_total h

                    INNER JOIN poblacion p
                            ON p.id_cartera_credito =
                               h.id_cartera_credito

                    WHERE h.fecha_corte =
                          :fechaCorte

                    ORDER BY
                        h.id_cartera_credito,
                        h.id_cierre_cartera DESC,
                        h.id_cierre_cartera_credito DESC
                ),

                base AS MATERIALIZED (

                    SELECT
                        p.id_cartera_credito,
                        p.id_agencia,
                        p.id_linea_credito,
                        p.codigo_linea_credito,
                        p.nombre_linea_credito,
                        p.pagare_cartera,

                        p.id_datos_personal,
                        p.tipo_documento,
                        p.documento,
                        p.nombre_completo,

                        p.fecha_desembolso,
                        p.cosecha,
                        p.valor_inicial_credito,
                        p.valor_desembolsado,

                        :fechaCorte::date
                            AS fecha_corte,

                        (
                            (
                                EXTRACT(
                                    YEAR FROM :fechaCorte::date
                                )::integer * 12
                            )
                            +
                            EXTRACT(
                                MONTH FROM :fechaCorte::date
                            )::integer
                        )
                        -
                        (
                            (
                                EXTRACT(
                                    YEAR FROM p.cosecha
                                )::integer * 12
                            )
                            +
                            EXTRACT(
                                MONTH FROM p.cosecha
                            )::integer
                        ) AS mob,

                        (
                            o.id_cartera_credito
                            IS NOT NULL
                        ) AS presente_corte,

                        COALESCE(
                            o.saldo_capital,
                            0
                        ) AS saldo_capital,

                        COALESCE(
                            o.dias_mora,
                            0
                        ) AS dias_mora,

                        o.codigo_estado_cartera,

                        o.descripcion_estado_cartera,

                        o.edad_riesgo_inicial,
                        o.edad_mora,
                        o.edad_riesgo,
                        o.edad_pe,
                        o.edad_homologacion,
                        o.edad_contable,

                        COALESCE(
                            o.vea,
                            0
                        ) AS vea,

                        COALESCE(
                            o.pi,
                            0
                        ) AS pi,

                        COALESCE(
                            o.pdi,
                            0
                        ) AS pdi,

                        COALESCE(
                            o.perdida_esperada,
                            0
                        ) AS perdida_esperada,

                        COALESCE(
                            o.deterioro_capital,
                            0
                        ) AS deterioro_capital,

                        COALESCE(
                            o.deterioro_intereses,
                            0
                        ) AS deterioro_intereses,

                        COALESCE(
                            o.deterioro_otros,
                            0
                        ) AS deterioro_otros,

                        COALESCE(
                            o.deterioro_total,
                            0
                        ) AS deterioro_total

                    FROM poblacion p

                    LEFT JOIN observacion o
                           ON o.id_cartera_credito =
                              p.id_cartera_credito
                ),

                seleccion AS MATERIALIZED (

                    SELECT
                        b.*

                    FROM base b

                    WHERE
                        CASE

                            WHEN :indicador = 'ORIGINADOS'
                                THEN TRUE

                            WHEN :indicador = 'PRESENTES'
                                THEN b.presente_corte

                            WHEN :indicador = 'SIN_PRESENCIA'
                                THEN NOT b.presente_corte

                            WHEN :indicador = 'CON_SALDO'
                                THEN b.presente_corte
                                 AND b.saldo_capital > 0

                            WHEN :indicador = 'SALDO'
                                THEN b.presente_corte
                                 AND b.saldo_capital > 0

                            WHEN :indicador = 'SALDO_REMANENTE'
                                THEN b.presente_corte
                                 AND b.saldo_capital > 0

                            WHEN :indicador = 'MORA_30'
                                THEN b.presente_corte
                                 AND b.saldo_capital > 0
                                 AND b.dias_mora >= 30

                            WHEN :indicador = 'MORA_60'
                                THEN b.presente_corte
                                 AND b.saldo_capital > 0
                                 AND b.dias_mora >= 60

                            WHEN :indicador = 'MORA_90'
                                THEN b.presente_corte
                                 AND b.saldo_capital > 0
                                 AND b.dias_mora >= 90

                            WHEN :indicador = 'MORA_180'
                                THEN b.presente_corte
                                 AND b.saldo_capital > 0
                                 AND b.dias_mora >= 180

                            ELSE FALSE
                        END
                )

                SELECT
                    s.id_cartera_credito,
                    s.id_agencia,
                    s.id_linea_credito,
                    s.codigo_linea_credito,
                    s.nombre_linea_credito,
                    s.pagare_cartera,

                    s.id_datos_personal,
                    s.tipo_documento,
                    s.documento,
                    s.nombre_completo,

                    dp.telefono,

                    dp.celular_uno
                        AS celular,

                    dp.correo_personal
                        AS correo,

                    s.fecha_desembolso,
                    s.cosecha,
                    s.valor_inicial_credito,
                    s.valor_desembolsado,

                    s.fecha_corte,
                    s.mob,
                    s.presente_corte,

                    s.saldo_capital,
                    s.dias_mora,

                    CASE
                        WHEN NOT s.presente_corte
                            THEN NULL

                        WHEN s.dias_mora <= 30
                            THEN 'A'

                        WHEN s.dias_mora <= 60
                            THEN 'B'

                        WHEN s.dias_mora <= 90
                            THEN 'C'

                        WHEN s.dias_mora <= 180
                            THEN 'D'

                        ELSE 'E'
                    END AS categoria_mora,

                    s.codigo_estado_cartera,

                    s.descripcion_estado_cartera,

                    s.edad_riesgo_inicial,
                    s.edad_mora,
                    s.edad_riesgo,
                    s.edad_pe,
                    s.edad_homologacion,
                    s.edad_contable,

                    s.vea,
                    s.pi,
                    s.pdi,
                    s.perdida_esperada,

                    s.deterioro_capital,
                    s.deterioro_intereses,
                    s.deterioro_otros,
                    s.deterioro_total,

                    (
                        s.presente_corte
                        AND s.saldo_capital > 0
                    ) AS con_saldo,

                    (
                        s.presente_corte
                        AND s.saldo_capital > 0
                        AND s.dias_mora >= 30
                    ) AS mora30,

                    (
                        s.presente_corte
                        AND s.saldo_capital > 0
                        AND s.dias_mora >= 60
                    ) AS mora60,

                    (
                        s.presente_corte
                        AND s.saldo_capital > 0
                        AND s.dias_mora >= 90
                    ) AS mora90,

                    (
                        s.presente_corte
                        AND s.saldo_capital > 0
                        AND s.dias_mora >= 180
                    ) AS mora180

                FROM seleccion s

                LEFT JOIN reporting.vw_datos_personales_operativa dp
                       ON dp.id_datos_personal =
                          s.id_datos_personal

                ORDER BY
                    s.id_agencia,
                    s.nombre_completo,
                    s.pagare_cartera,
                    s.id_cartera_credito
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "cosecha",
                                cosecha
                        )
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        )
                        .addValue(
                                "indicador",
                                indicador
                        )
                        .addValue(
                                "idAgencia",
                                idAgencia
                        )
                        .addValue(
                                "idLineaCredito",
                                idLineaCredito
                        );

        return jdbc.query(
                sql,
                params,
                new BeanPropertyRowMapper<>(
                        CosechaDetalleDTO.class
                )
        );
    }
}