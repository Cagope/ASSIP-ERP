package co.assip.erp.cartera.analisis.riesgodeterioro;

import co.assip.erp.cartera.analisis.riesgodeterioro.dto.*;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Repository
public class AnalisisRiesgoDeterioroRepository {

    private static final String VISTA =
            "cartera.vw_cartera_resultados_mensuales_total";

    private final NamedParameterJdbcTemplate jdbc;

    public AnalisisRiesgoDeterioroRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    // =========================================================
    // CORTES
    // =========================================================

    public List<LocalDate> listarCortes() {

        String sql = """
                SELECT DISTINCT
                    fecha_corte
                FROM %s
                WHERE fecha_corte IS NOT NULL
                ORDER BY fecha_corte DESC
                """.formatted(VISTA);

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                (rs, rowNum) ->
                        rs.getObject(
                                "fecha_corte",
                                LocalDate.class
                        )
        );
    }


    // =========================================================
    // RESUMEN
    // =========================================================

    public RiesgoDeterioroResumenDTO consultarResumen(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        String sql = """
                SELECT
                    :fechaCorte::date AS fecha_corte,

                    COUNT(DISTINCT h.id_cartera_credito)::int
                        AS cantidad_creditos,

                    COALESCE(
                        SUM(h.saldo_credito_fecha_corte),
                        0
                    ) AS saldo_cartera,

                    COALESCE(
                        SUM(h.vea),
                        0
                    ) AS vea,

                    COALESCE(
                        SUM(h.exposicion_total_calculada),
                        0
                    ) AS exposicion_total,

                    COALESCE(
                        SUM(h.perdida_esperada),
                        0
                    ) AS perdida_esperada,

                    COALESCE(
                        SUM(h.deterioro_capital),
                        0
                    ) AS deterioro_capital,

                    COALESCE(
                        SUM(h.deterioro_intereses),
                        0
                    ) AS deterioro_intereses,

                    COALESCE(
                        SUM(h.deterioro_otros),
                        0
                    ) AS deterioro_otros,

                    COALESCE(
                        SUM(h.deterioro_total),
                        0
                    ) AS deterioro_total,


                    CASE
                        WHEN COALESCE(SUM(h.vea), 0) = 0
                            THEN 0
                        ELSE
                            ROUND(
                                SUM(h.perdida_esperada)
                                * 100.0
                                /
                                SUM(h.vea),
                                4
                            )
                    END
                        AS porcentaje_perdida_esperada_sobre_vea,


                    CASE
                        WHEN COALESCE(
                            SUM(h.saldo_credito_fecha_corte),
                            0
                        ) = 0
                            THEN 0
                        ELSE
                            ROUND(
                                SUM(h.deterioro_total)
                                * 100.0
                                /
                                SUM(h.saldo_credito_fecha_corte),
                                4
                            )
                    END
                        AS porcentaje_deterioro_sobre_saldo,


                    COUNT(
                        DISTINCT h.id_cartera_credito
                    ) FILTER (
                        WHERE COALESCE(h.dias_mora, 0) >= 30
                    )::int
                        AS cantidad_mora30,

                    COALESCE(
                        SUM(h.saldo_credito_fecha_corte)
                        FILTER (
                            WHERE COALESCE(h.dias_mora, 0) >= 30
                        ),
                        0
                    ) AS saldo_mora30,

                    CASE
                        WHEN COALESCE(
                            SUM(h.saldo_credito_fecha_corte),
                            0
                        ) = 0
                            THEN 0
                        ELSE
                            ROUND(
                                COALESCE(
                                    SUM(h.saldo_credito_fecha_corte)
                                    FILTER (
                                        WHERE COALESCE(h.dias_mora, 0) >= 30
                                    ),
                                    0
                                )
                                * 100.0
                                /
                                SUM(h.saldo_credito_fecha_corte),
                                4
                            )
                    END
                        AS porcentaje_saldo_mora30,


                    COUNT(
                        DISTINCT h.id_cartera_credito
                    ) FILTER (
                        WHERE COALESCE(h.dias_mora, 0) >= 60
                    )::int
                        AS cantidad_mora60,

                    COALESCE(
                        SUM(h.saldo_credito_fecha_corte)
                        FILTER (
                            WHERE COALESCE(h.dias_mora, 0) >= 60
                        ),
                        0
                    ) AS saldo_mora60,

                    CASE
                        WHEN COALESCE(
                            SUM(h.saldo_credito_fecha_corte),
                            0
                        ) = 0
                            THEN 0
                        ELSE
                            ROUND(
                                COALESCE(
                                    SUM(h.saldo_credito_fecha_corte)
                                    FILTER (
                                        WHERE COALESCE(h.dias_mora, 0) >= 60
                                    ),
                                    0
                                )
                                * 100.0
                                /
                                SUM(h.saldo_credito_fecha_corte),
                                4
                            )
                    END
                        AS porcentaje_saldo_mora60,


                    COUNT(
                        DISTINCT h.id_cartera_credito
                    ) FILTER (
                        WHERE COALESCE(h.dias_mora, 0) >= 90
                    )::int
                        AS cantidad_mora90,

                    COALESCE(
                        SUM(h.saldo_credito_fecha_corte)
                        FILTER (
                            WHERE COALESCE(h.dias_mora, 0) >= 90
                        ),
                        0
                    ) AS saldo_mora90,

                    CASE
                        WHEN COALESCE(
                            SUM(h.saldo_credito_fecha_corte),
                            0
                        ) = 0
                            THEN 0
                        ELSE
                            ROUND(
                                COALESCE(
                                    SUM(h.saldo_credito_fecha_corte)
                                    FILTER (
                                        WHERE COALESCE(h.dias_mora, 0) >= 90
                                    ),
                                    0
                                )
                                * 100.0
                                /
                                SUM(h.saldo_credito_fecha_corte),
                                4
                            )
                    END
                        AS porcentaje_saldo_mora90,


                    COUNT(
                        DISTINCT h.id_cartera_credito
                    ) FILTER (
                        WHERE COALESCE(h.dias_mora, 0) >= 180
                    )::int
                        AS cantidad_mora180,

                    COALESCE(
                        SUM(h.saldo_credito_fecha_corte)
                        FILTER (
                            WHERE COALESCE(h.dias_mora, 0) >= 180
                        ),
                        0
                    ) AS saldo_mora180,

                    CASE
                        WHEN COALESCE(
                            SUM(h.saldo_credito_fecha_corte),
                            0
                        ) = 0
                            THEN 0
                        ELSE
                            ROUND(
                                COALESCE(
                                    SUM(h.saldo_credito_fecha_corte)
                                    FILTER (
                                        WHERE COALESCE(h.dias_mora, 0) >= 180
                                    ),
                                    0
                                )
                                * 100.0
                                /
                                SUM(h.saldo_credito_fecha_corte),
                                4
                            )
                    END
                        AS porcentaje_saldo_mora180

                FROM %s h

                WHERE h.fecha_corte = :fechaCorte

                  AND (
                      CAST(:idAgencia AS integer) IS NULL
                      OR h.id_agencia =
                         CAST(:idAgencia AS integer)
                  )

                  AND (
                      CAST(:idLineaCredito AS integer) IS NULL
                      OR h.id_linea_credito =
                         CAST(:idLineaCredito AS integer)
                  )
                """.formatted(VISTA);

        return jdbc.queryForObject(
                sql,
                parametros(
                        fechaCorte,
                        idAgencia,
                        idLineaCredito
                ),
                new BeanPropertyRowMapper<>(
                        RiesgoDeterioroResumenDTO.class
                )
        );
    }


    // =========================================================
    // EDADES
    // =========================================================

    public List<RiesgoDeterioroEdadDTO> consultarEdades(
            LocalDate fechaCorte,
            String tipoEdad,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        String columnaEdad =
                resolverColumnaEdad(tipoEdad);

        String sql = """
                WITH base AS (
                    SELECT
                        COALESCE(
                            NULLIF(TRIM(%s), ''),
                            'SIN EDAD'
                        ) AS edad,

                        id_cartera_credito,

                        COALESCE(
                            saldo_credito_fecha_corte,
                            0
                        ) AS saldo,

                        COALESCE(vea, 0) AS vea,

                        COALESCE(
                            exposicion_total_calculada,
                            0
                        ) AS exposicion,

                        COALESCE(pi, 0) AS pi,
                        COALESCE(pdi, 0) AS pdi,

                        COALESCE(
                            perdida_esperada,
                            0
                        ) AS perdida_esperada,

                        COALESCE(
                            deterioro_capital,
                            0
                        ) AS deterioro_capital,

                        COALESCE(
                            deterioro_intereses,
                            0
                        ) AS deterioro_intereses,

                        COALESCE(
                            deterioro_otros,
                            0
                        ) AS deterioro_otros,

                        COALESCE(
                            deterioro_total,
                            0
                        ) AS deterioro_total

                    FROM %s

                    WHERE fecha_corte = :fechaCorte

                      AND (
                          CAST(:idAgencia AS integer) IS NULL
                          OR id_agencia =
                             CAST(:idAgencia AS integer)
                      )

                      AND (
                          CAST(:idLineaCredito AS integer) IS NULL
                          OR id_linea_credito =
                             CAST(:idLineaCredito AS integer)
                      )
                ),

                total AS (
                    SELECT
                        COALESCE(SUM(saldo), 0)
                            AS saldo_total
                    FROM base
                )

                SELECT
                    b.edad,

                    COUNT(
                        DISTINCT b.id_cartera_credito
                    )::int
                        AS cantidad_creditos,

                    SUM(b.saldo)
                        AS saldo_cartera,

                    CASE
                        WHEN t.saldo_total = 0
                            THEN 0
                        ELSE
                            ROUND(
                                SUM(b.saldo)
                                * 100.0
                                /
                                t.saldo_total,
                                4
                            )
                    END
                        AS porcentaje_saldo,

                    SUM(b.vea)
                        AS vea,

                    SUM(b.exposicion)
                        AS exposicion_total,

                    CASE
                        WHEN SUM(b.vea) = 0
                            THEN 0
                        ELSE
                            ROUND(
                                SUM(
                                    b.pi * b.vea
                                )
                                /
                                SUM(b.vea),
                                6
                            )
                    END
                        AS pi_promedio_ponderado,

                    CASE
                        WHEN SUM(b.vea) = 0
                            THEN 0
                        ELSE
                            ROUND(
                                SUM(
                                    b.pdi * b.vea
                                )
                                /
                                SUM(b.vea),
                                6
                            )
                    END
                        AS pdi_promedio_ponderado,

                    SUM(b.perdida_esperada)
                        AS perdida_esperada,

                    SUM(b.deterioro_capital)
                        AS deterioro_capital,

                    SUM(b.deterioro_intereses)
                        AS deterioro_intereses,

                    SUM(b.deterioro_otros)
                        AS deterioro_otros,

                    SUM(b.deterioro_total)
                        AS deterioro_total

                FROM base b

                CROSS JOIN total t

                GROUP BY
                    b.edad,
                    t.saldo_total

                ORDER BY
                    CASE b.edad
                        WHEN 'A' THEN 1
                        WHEN 'B' THEN 2
                        WHEN 'C' THEN 3
                        WHEN 'D' THEN 4
                        WHEN 'E' THEN 5
                        ELSE 99
                    END,
                    b.edad
                """.formatted(
                columnaEdad,
                VISTA
        );

        return jdbc.query(
                sql,
                parametros(
                        fechaCorte,
                        idAgencia,
                        idLineaCredito
                ),
                new BeanPropertyRowMapper<>(
                        RiesgoDeterioroEdadDTO.class
                )
        );
    }


    // =========================================================
    // SEGMENTACIÓN
    // =========================================================

    public List<RiesgoDeterioroSegmentoDTO> consultarSegmentacion(
            LocalDate fechaCorte,
            String dimension,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        DimensionSql d =
                resolverDimension(dimension);

        String sql = """
                WITH base AS (
                    SELECT
                        COALESCE(
                            NULLIF(
                                TRIM(%s),
                                ''
                            ),
                            'SIN CÓDIGO'
                        ) AS codigo,

                        COALESCE(
                            NULLIF(
                                TRIM(%s),
                                ''
                            ),
                            'SIN INFORMACIÓN'
                        ) AS descripcion,

                        id_cartera_credito,

                        COALESCE(
                            saldo_credito_fecha_corte,
                            0
                        ) AS saldo,

                        COALESCE(vea, 0)
                            AS vea,

                        COALESCE(
                            exposicion_total_calculada,
                            0
                        ) AS exposicion,

                        COALESCE(
                            perdida_esperada,
                            0
                        ) AS perdida_esperada,

                        COALESCE(
                            deterioro_capital,
                            0
                        ) AS deterioro_capital,

                        COALESCE(
                            deterioro_intereses,
                            0
                        ) AS deterioro_intereses,

                        COALESCE(
                            deterioro_otros,
                            0
                        ) AS deterioro_otros,

                        COALESCE(
                            deterioro_total,
                            0
                        ) AS deterioro_total

                    FROM %s

                    WHERE fecha_corte = :fechaCorte

                      AND (
                          CAST(:idAgencia AS integer) IS NULL
                          OR id_agencia =
                             CAST(:idAgencia AS integer)
                      )

                      AND (
                          CAST(:idLineaCredito AS integer) IS NULL
                          OR id_linea_credito =
                             CAST(:idLineaCredito AS integer)
                      )
                ),

                total AS (
                    SELECT
                        COALESCE(
                            SUM(saldo),
                            0
                        ) AS saldo_total
                    FROM base
                )

                SELECT
                    b.codigo,
                    b.descripcion,

                    COUNT(
                        DISTINCT b.id_cartera_credito
                    )::int
                        AS cantidad_creditos,

                    SUM(b.saldo)
                        AS saldo_cartera,

                    CASE
                        WHEN t.saldo_total = 0
                            THEN 0
                        ELSE
                            ROUND(
                                SUM(b.saldo)
                                * 100.0
                                /
                                t.saldo_total,
                                4
                            )
                    END
                        AS porcentaje_saldo,

                    SUM(b.vea)
                        AS vea,

                    SUM(b.exposicion)
                        AS exposicion_total,

                    SUM(b.perdida_esperada)
                        AS perdida_esperada,

                    SUM(b.deterioro_capital)
                        AS deterioro_capital,

                    SUM(b.deterioro_intereses)
                        AS deterioro_intereses,

                    SUM(b.deterioro_otros)
                        AS deterioro_otros,

                    SUM(b.deterioro_total)
                        AS deterioro_total,

                    CASE
                        WHEN SUM(b.saldo) = 0
                            THEN 0
                        ELSE
                            ROUND(
                                SUM(b.deterioro_total)
                                * 100.0
                                /
                                SUM(b.saldo),
                                4
                            )
                    END
                        AS porcentaje_deterioro_sobre_saldo

                FROM base b

                CROSS JOIN total t

                GROUP BY
                    b.codigo,
                    b.descripcion,
                    t.saldo_total

                ORDER BY
                    saldo_cartera DESC,
                    b.codigo
                """.formatted(
                d.codigo(),
                d.descripcion(),
                VISTA
        );

        return jdbc.query(
                sql,
                parametros(
                        fechaCorte,
                        idAgencia,
                        idLineaCredito
                ),
                new BeanPropertyRowMapper<>(
                        RiesgoDeterioroSegmentoDTO.class
                )
        );
    }


    // =========================================================
    // EVOLUCIÓN
    // =========================================================

    public List<RiesgoDeterioroEvolucionDTO> consultarEvolucion(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        String sql = """
                WITH mensual AS (
                    SELECT
                        h.fecha_corte,

                        COUNT(
                            DISTINCT h.id_cartera_credito
                        )::int
                            AS cantidad_creditos,

                        COALESCE(
                            SUM(h.saldo_credito_fecha_corte),
                            0
                        ) AS saldo_cartera,

                        COALESCE(
                            SUM(h.vea),
                            0
                        ) AS vea,

                        COALESCE(
                            SUM(h.exposicion_total_calculada),
                            0
                        ) AS exposicion_total,

                        COALESCE(
                            SUM(h.perdida_esperada),
                            0
                        ) AS perdida_esperada,

                        COALESCE(
                            SUM(h.deterioro_total),
                            0
                        ) AS deterioro_total,

                        COALESCE(
                            SUM(h.saldo_credito_fecha_corte)
                            FILTER (
                                WHERE COALESCE(h.dias_mora, 0) >= 30
                            ),
                            0
                        ) AS saldo_mora30,

                        COALESCE(
                            SUM(h.saldo_credito_fecha_corte)
                            FILTER (
                                WHERE COALESCE(h.dias_mora, 0) >= 60
                            ),
                            0
                        ) AS saldo_mora60,

                        COALESCE(
                            SUM(h.saldo_credito_fecha_corte)
                            FILTER (
                                WHERE COALESCE(h.dias_mora, 0) >= 90
                            ),
                            0
                        ) AS saldo_mora90,

                        COALESCE(
                            SUM(h.saldo_credito_fecha_corte)
                            FILTER (
                                WHERE COALESCE(h.dias_mora, 0) >= 180
                            ),
                            0
                        ) AS saldo_mora180

                    FROM %s h

                    WHERE h.fecha_corte
                          BETWEEN :fechaDesde
                              AND :fechaHasta

                      AND (
                          CAST(:idAgencia AS integer) IS NULL
                          OR h.id_agencia =
                             CAST(:idAgencia AS integer)
                      )

                      AND (
                          CAST(:idLineaCredito AS integer) IS NULL
                          OR h.id_linea_credito =
                             CAST(:idLineaCredito AS integer)
                      )

                    GROUP BY
                        h.fecha_corte
                ),

                calculado AS (
                    SELECT
                        m.*,

                        CASE
                            WHEN m.saldo_cartera = 0
                                THEN 0
                            ELSE
                                ROUND(
                                    m.saldo_mora30
                                    * 100.0
                                    /
                                    m.saldo_cartera,
                                    4
                                )
                        END
                            AS porcentaje_mora30,

                        CASE
                            WHEN m.saldo_cartera = 0
                                THEN 0
                            ELSE
                                ROUND(
                                    m.saldo_mora60
                                    * 100.0
                                    /
                                    m.saldo_cartera,
                                    4
                                )
                        END
                            AS porcentaje_mora60,

                        CASE
                            WHEN m.saldo_cartera = 0
                                THEN 0
                            ELSE
                                ROUND(
                                    m.saldo_mora90
                                    * 100.0
                                    /
                                    m.saldo_cartera,
                                    4
                                )
                        END
                            AS porcentaje_mora90,

                        CASE
                            WHEN m.saldo_cartera = 0
                                THEN 0
                            ELSE
                                ROUND(
                                    m.saldo_mora180
                                    * 100.0
                                    /
                                    m.saldo_cartera,
                                    4
                                )
                        END
                            AS porcentaje_mora180,

                        m.saldo_cartera
                        -
                        LAG(m.saldo_cartera)
                        OVER (
                            ORDER BY m.fecha_corte
                        )
                            AS variacion_saldo,

                        m.perdida_esperada
                        -
                        LAG(m.perdida_esperada)
                        OVER (
                            ORDER BY m.fecha_corte
                        )
                            AS variacion_perdida_esperada,

                        m.deterioro_total
                        -
                        LAG(m.deterioro_total)
                        OVER (
                            ORDER BY m.fecha_corte
                        )
                            AS variacion_deterioro,

                        LAG(m.saldo_cartera)
                        OVER (
                            ORDER BY m.fecha_corte
                        )
                            AS saldo_anterior,

                        LAG(m.deterioro_total)
                        OVER (
                            ORDER BY m.fecha_corte
                        )
                            AS deterioro_anterior

                    FROM mensual m
                )

                SELECT
                    fecha_corte,
                    cantidad_creditos,
                    saldo_cartera,
                    vea,
                    exposicion_total,
                    perdida_esperada,
                    deterioro_total,

                    saldo_mora30,
                    saldo_mora60,
                    saldo_mora90,
                    saldo_mora180,

                    porcentaje_mora30,
                    porcentaje_mora60,
                    porcentaje_mora90,
                    porcentaje_mora180,

                    COALESCE(
                        variacion_saldo,
                        0
                    ) AS variacion_saldo,

                    CASE
                        WHEN COALESCE(
                            saldo_anterior,
                            0
                        ) = 0
                            THEN 0
                        ELSE
                            ROUND(
                                variacion_saldo
                                * 100.0
                                /
                                saldo_anterior,
                                4
                            )
                    END
                        AS variacion_saldo_porcentaje,

                    COALESCE(
                        variacion_perdida_esperada,
                        0
                    ) AS variacion_perdida_esperada,

                    COALESCE(
                        variacion_deterioro,
                        0
                    ) AS variacion_deterioro,

                    CASE
                        WHEN COALESCE(
                            deterioro_anterior,
                            0
                        ) = 0
                            THEN 0
                        ELSE
                            ROUND(
                                variacion_deterioro
                                * 100.0
                                /
                                deterioro_anterior,
                                4
                            )
                    END
                        AS variacion_deterioro_porcentaje

                FROM calculado

                ORDER BY fecha_corte
                """.formatted(VISTA);

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaDesde",
                                fechaDesde
                        )
                        .addValue(
                                "fechaHasta",
                                fechaHasta
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
                        RiesgoDeterioroEvolucionDTO.class
                )
        );
    }


    // =========================================================
    // CONCENTRACIÓN
    // =========================================================

    public List<RiesgoDeterioroConcentracionDTO> consultarConcentracion(
            LocalDate fechaCorte,
            String criterio,
            Integer limite,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        String columnaOrden =
                resolverColumnaConcentracion(
                        criterio
                );

        int limiteSeguro =
                Math.min(
                        Math.max(
                                limite == null
                                        ? 20
                                        : limite,
                                1
                        ),
                        500
                );

        String sql = """
                WITH base AS (
                    SELECT
                        h.id_cartera_credito,
                        h.id_datos_personal,

                        h.id_agencia,

                        h.id_linea_credito,
                        h.codigo_linea_credito,
                        h.nombre_linea_credito,

                        h.pagare_cartera,

                        h.tipo_documento,
                        h.documento,
                        h.nombre_completo,

                        COALESCE(
                            h.saldo_credito_fecha_corte,
                            0
                        ) AS saldo_cartera,

                        COALESCE(
                            h.dias_mora,
                            0
                        ) AS dias_mora,

                        h.edad_contable_resultado
                            AS edad_contable,

                        COALESCE(h.vea, 0)
                            AS vea,

                        COALESCE(h.pi, 0)
                            AS pi,

                        COALESCE(h.pdi, 0)
                            AS pdi,

                        COALESCE(
                            h.perdida_esperada,
                            0
                        ) AS perdida_esperada,

                        COALESCE(
                            h.deterioro_total,
                            0
                        ) AS deterioro_total,

                        COALESCE(
                            h.exposicion_total_calculada,
                            0
                        ) AS exposicion_total,

                        COALESCE(
                            h.valor_aportes_credito,
                            0
                        ) AS valor_aportes_credito,

                        COALESCE(
                            h.valor_garantias_credito,
                            0
                        ) AS valor_garantias_credito

                    FROM %s h

                    WHERE h.fecha_corte = :fechaCorte

                      AND (
                          CAST(:idAgencia AS integer) IS NULL
                          OR h.id_agencia =
                             CAST(:idAgencia AS integer)
                      )

                      AND (
                          CAST(:idLineaCredito AS integer) IS NULL
                          OR h.id_linea_credito =
                             CAST(:idLineaCredito AS integer)
                      )
                ),

                total AS (
                    SELECT
                        COALESCE(
                            SUM(%s),
                            0
                        ) AS total_criterio
                    FROM base
                ),

                ranking AS (
                    SELECT
                        ROW_NUMBER()
                        OVER (
                            ORDER BY
                                %s DESC,
                                id_cartera_credito
                        )::int
                            AS posicion,

                        b.*,

                        t.total_criterio,

                        CASE
                            WHEN t.total_criterio = 0
                                THEN 0
                            ELSE
                                ROUND(
                                    %s
                                    * 100.0
                                    /
                                    t.total_criterio,
                                    6
                                )
                        END
                            AS porcentaje_sobre_total

                    FROM base b

                    CROSS JOIN total t
                ),

                acumulado AS (
                    SELECT
                        r.*,

                        SUM(
                            porcentaje_sobre_total
                        )
                        OVER (
                            ORDER BY posicion
                            ROWS BETWEEN
                                UNBOUNDED PRECEDING
                                AND CURRENT ROW
                        )
                            AS porcentaje_acumulado

                    FROM ranking r
                )

                SELECT
                    posicion,

                    id_cartera_credito,
                    id_datos_personal,

                    id_agencia,

                    id_linea_credito,
                    codigo_linea_credito,
                    nombre_linea_credito,

                    pagare_cartera,

                    tipo_documento,
                    documento,
                    nombre_completo,

                    saldo_cartera,

                    dias_mora,
                    edad_contable,

                    vea,
                    pi,
                    pdi,

                    perdida_esperada,
                    deterioro_total,
                    exposicion_total,

                    valor_aportes_credito,
                    valor_garantias_credito,

                    porcentaje_sobre_total,
                    porcentaje_acumulado

                FROM acumulado

                ORDER BY posicion

                LIMIT :limite
                """.formatted(
                VISTA,
                columnaOrden,
                columnaOrden,
                columnaOrden
        );

        MapSqlParameterSource params =
                parametros(
                        fechaCorte,
                        idAgencia,
                        idLineaCredito
                )
                        .addValue(
                                "limite",
                                limiteSeguro
                        );

        return jdbc.query(
                sql,
                params,
                new BeanPropertyRowMapper<>(
                        RiesgoDeterioroConcentracionDTO.class
                )
        );
    }


    // =========================================================
    // DETALLE GENERAL
    // =========================================================

    public List<RiesgoDeterioroDetalleDTO> consultarDetalle(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        String sql = """
                SELECT
                    h.id_cartera_credito,
                    h.id_cierre_cartera_credito,

                    h.id_agencia,

                    h.id_linea_credito,
                    h.codigo_linea_credito,
                    h.nombre_linea_credito,

                    h.pagare_cartera,

                    h.id_datos_personal,
                    h.tipo_documento,
                    h.documento,
                    h.nombre_completo,

                    h.codigo_clasificacion_credito,
                    h.descripcion_clasificacion_credito,

                    h.codigo_garantia_credito,
                    h.descripcion_garantia_credito,
                    h.tipo_garantia,

                    h.codigo_destino_economico,
                    h.descripcion_destino_economico,

                    h.codigo_estado_cartera,
                    h.descripcion_estado_cartera,

                    h.codigo_estado_juridico,
                    h.descripcion_estado_juridico,

                    h.codigo_modificacion_credito,
                    h.descripcion_modificacion_credito,

                    h.es_reestructurado,

                    h.fecha_desembolso,
                    h.fecha_corte,

                    COALESCE(
                        h.valor_inicial_credito,
                        0
                    ) AS valor_inicial_credito,

                    COALESCE(
                        h.valor_desembolsado,
                        0
                    ) AS valor_desembolsado,

                    COALESCE(
                        h.saldo_credito_fecha_corte,
                        0
                    ) AS saldo_cartera,

                    COALESCE(
                        h.dias_mora,
                        0
                    ) AS dias_mora,

                    h.edad_riesgo_inicial_resultado
                        AS edad_riesgo_inicial,

                    h.edad_de_mora_resultado
                        AS edad_mora,

                    h.edad_de_riesgo_resultado
                        AS edad_riesgo,

                    h.edad_de_pe_resultado
                        AS edad_pe,

                    h.edad_de_homologacion_resultado
                        AS edad_homologacion,

                    h.edad_contable_resultado
                        AS edad_contable,

                    COALESCE(
                        h.saldo_aportes_fecha_corte,
                        0
                    ) AS saldo_aportes_fecha_corte,

                    COALESCE(
                        h.porcentaje_aportes_credito,
                        0
                    ) AS porcentaje_aportes_credito,

                    COALESCE(
                        h.valor_aportes_credito,
                        0
                    ) AS valor_aportes_credito,

                    COALESCE(
                        h.cantidad_bienes_garantia,
                        0
                    ) AS cantidad_bienes_garantia,

                    COALESCE(
                        h.valor_garantias_total,
                        0
                    ) AS valor_garantias_total,

                    COALESCE(
                        h.porcentaje_garantias_credito,
                        0
                    ) AS porcentaje_garantias_credito,

                    COALESCE(
                        h.valor_garantias_credito,
                        0
                    ) AS valor_garantias_credito,

                    COALESCE(
                        h.vea,
                        0
                    ) AS vea,

                    COALESCE(
                        h.saldo_intereses_causados,
                        0
                    ) AS saldo_intereses_causados,

                    COALESCE(
                        h.saldo_intereses_contingentes,
                        0
                    ) AS saldo_intereses_contingentes,

                    COALESCE(
                        h.valor_costas_judiciales,
                        0
                    ) AS valor_costas_judiciales,

                    COALESCE(
                        h.saldo_seguros,
                        0
                    ) AS saldo_seguros,

                    COALESCE(
                        h.saldo_alivios,
                        0
                    ) AS saldo_alivios,

                    COALESCE(
                        h.valor_fondos_garantias,
                        0
                    ) AS valor_fondos_garantias,

                    COALESCE(
                        h.valor_otros_conceptos,
                        0
                    ) AS valor_otros_conceptos,

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
                    ) AS deterioro_total,

                    COALESCE(
                        h.exposicion_total_calculada,
                        0
                    ) AS exposicion_total

                FROM %s h

                WHERE h.fecha_corte = :fechaCorte

                  AND (
                      CAST(:idAgencia AS integer) IS NULL
                      OR h.id_agencia =
                         CAST(:idAgencia AS integer)
                  )

                  AND (
                      CAST(:idLineaCredito AS integer) IS NULL
                      OR h.id_linea_credito =
                         CAST(:idLineaCredito AS integer)
                  )

                ORDER BY
                    h.deterioro_total DESC NULLS LAST,
                    h.saldo_credito_fecha_corte DESC NULLS LAST,
                    h.id_cartera_credito
                """.formatted(VISTA);

        return jdbc.query(
                sql,
                parametros(
                        fechaCorte,
                        idAgencia,
                        idLineaCredito
                ),
                new BeanPropertyRowMapper<>(
                        RiesgoDeterioroDetalleDTO.class
                )
        );
    }


    // =========================================================
    // PARÁMETROS
    // =========================================================

    private MapSqlParameterSource parametros(
            LocalDate fechaCorte,
            Integer idAgencia,
            Integer idLineaCredito
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        "fechaCorte",
                        fechaCorte
                )
                .addValue(
                        "idAgencia",
                        idAgencia
                )
                .addValue(
                        "idLineaCredito",
                        idLineaCredito
                );
    }


    // =========================================================
    // EDAD
    // =========================================================

    private String resolverColumnaEdad(
            String tipoEdad
    ) {

        String valor =
                normalizar(
                        tipoEdad
                );

        return switch (valor) {

            case "MORA" ->
                    "edad_de_mora_resultado";

            case "RIESGO" ->
                    "edad_de_riesgo_resultado";

            case "PE" ->
                    "edad_de_pe_resultado";

            case "HOMOLOGACION" ->
                    "edad_de_homologacion_resultado";

            case "CONTABLE" ->
                    "edad_contable_resultado";

            default ->
                    throw new IllegalArgumentException(
                            "Tipo de edad no válido: "
                                    + tipoEdad
                    );
        };
    }


    // =========================================================
    // DIMENSIÓN
    // =========================================================

    private DimensionSql resolverDimension(
            String dimension
    ) {

        String valor =
                normalizar(
                        dimension
                );

        return switch (valor) {

            case "AGENCIA" ->
                    new DimensionSql(
                            "id_agencia::text",
                            "'AGENCIA ' || id_agencia::text"
                    );

            case "LINEA" ->
                    new DimensionSql(
                            "codigo_linea_credito",
                            "nombre_linea_credito"
                    );

            case "CLASIFICACION" ->
                    new DimensionSql(
                            "codigo_clasificacion_credito",
                            "descripcion_clasificacion_credito"
                    );

            case "GARANTIA" ->
                    new DimensionSql(
                            "codigo_garantia_credito",
                            "descripcion_garantia_credito"
                    );

            case "DESTINO" ->
                    new DimensionSql(
                            "codigo_destino_economico",
                            "descripcion_destino_economico"
                    );

            case "ESTADO_JURIDICO" ->
                    new DimensionSql(
                            "codigo_estado_juridico",
                            "descripcion_estado_juridico"
                    );

            case "MODIFICACION" ->
                    new DimensionSql(
                            "codigo_modificacion_credito",
                            "descripcion_modificacion_credito"
                    );

            case "METODO_CALCULO" ->
                    new DimensionSql(
                            "codigo_metodo_calculo",
                            "codigo_metodo_calculo"
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Dimensión no válida: "
                                    + dimension
                    );
        };
    }


    // =========================================================
    // CONCENTRACIÓN
    // =========================================================

    private String resolverColumnaConcentracion(
            String criterio
    ) {

        String valor =
                normalizar(
                        criterio
                );

        return switch (valor) {

            case "SALDO" ->
                    "saldo_cartera";

            case "PERDIDA_ESPERADA" ->
                    "perdida_esperada";

            case "DETERIORO" ->
                    "deterioro_total";

            case "EXPOSICION" ->
                    "exposicion_total";

            default ->
                    throw new IllegalArgumentException(
                            "Criterio de concentración no válido: "
                                    + criterio
                    );
        };
    }


    private String normalizar(
            String valor
    ) {

        if (valor == null) {
            return "";
        }

        return valor
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }


    private record DimensionSql(
            String codigo,
            String descripcion
    ) {
    }
}