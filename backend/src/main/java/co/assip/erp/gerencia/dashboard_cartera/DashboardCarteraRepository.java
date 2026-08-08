package co.assip.erp.gerencia.dashboard_cartera;

import co.assip.erp.gerencia.dashboard_cartera.dto.DashboardCarteraAgenciaDTO;
import co.assip.erp.gerencia.dashboard_cartera.dto.DashboardCarteraAlertaDTO;
import co.assip.erp.gerencia.dashboard_cartera.dto.DashboardCarteraLineaDTO;
import co.assip.erp.gerencia.dashboard_cartera.dto.DashboardCarteraRecaudoDTO;
import co.assip.erp.gerencia.dashboard_cartera.dto.DashboardCarteraRequestDTO;
import co.assip.erp.gerencia.dashboard_cartera.dto.DashboardCarteraResumenDTO;
import co.assip.erp.gerencia.dashboard_cartera.dto.DashboardCarteraRiesgoDTO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class DashboardCarteraRepository {

    private static final String VISTA_CREDITOS =
            "cartera.vw_cartera_creditos_total";

    private static final String VISTA_EXTRACTOS =
            "cartera.vw_cartera_extractos_total";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DashboardCarteraRepository(
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================================================
    // Resumen ejecutivo
    // =========================================================

    public DashboardCarteraResumenDTO consultarResumen(
            DashboardCarteraRequestDTO filtro
    ) {

        DashboardCarteraRequestDTO filtroSeguro =
                normalizarFiltro(filtro);

        MapSqlParameterSource parametros =
                construirParametros(filtroSeguro);

        String sql = """
            WITH creditos_filtrados AS (
                SELECT
                    c.id_cartera_credito,
                    c.id_datos_personal,

                    COALESCE(
                        c.saldo_actual,
                        0
                    ) AS saldo_actual,

                    COALESCE(
                        c.saldo_neto_pendiente,
                        0
                    ) AS saldo_neto_pendiente,

                    COALESCE(
                        c.credito_en_mora,
                        FALSE
                    ) AS credito_en_mora,

                    COALESCE(
                        c.riesgo_alto,
                        FALSE
                    ) AS riesgo_alto,

                    COALESCE(
                        c.mora_alta,
                        FALSE
                    ) AS mora_alta,

                    COALESCE(
                        c.riesgo_deteriorado_desde_inicial,
                        FALSE
                    ) AS riesgo_deteriorado_desde_inicial,

                    COALESCE(
                        c.riesgo_mejorado_desde_inicial,
                        FALSE
                    ) AS riesgo_mejorado_desde_inicial,

                    c.codigo_estado_juridico,
                    c.codigo_modificacion_credito

                FROM %s c

                WHERE 1 = 1
                %s
            ),
            resumen_creditos AS (
                SELECT
                    COALESCE(
                        SUM(c.saldo_actual),
                        0
                    ) AS saldo_cartera,

                    COALESCE(
                        SUM(c.saldo_neto_pendiente),
                        0
                    ) AS saldo_neto_pendiente,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN c.credito_en_mora = TRUE
                                    THEN c.saldo_actual
                                ELSE 0
                            END
                        ),
                        0
                    ) AS saldo_creditos_mora,

                    COUNT(
                        DISTINCT c.id_cartera_credito
                    ) AS cantidad_creditos_con_saldo,

                    COUNT(
                        DISTINCT c.id_datos_personal
                    ) AS cantidad_asociados,

                    COUNT(
                        DISTINCT c.id_cartera_credito
                    ) FILTER (
                        WHERE c.credito_en_mora = TRUE
                    ) AS cantidad_creditos_mora,

                    COUNT(
                        DISTINCT c.id_cartera_credito
                    ) FILTER (
                        WHERE
                            c.riesgo_alto = TRUE
                            OR c.mora_alta = TRUE
                    ) AS cantidad_creditos_criticos,

                    COUNT(
                        DISTINCT c.id_cartera_credito
                    ) FILTER (
                        WHERE
                            c.riesgo_deteriorado_desde_inicial = TRUE
                    ) AS cantidad_deteriorados,

                    COUNT(
                        DISTINCT c.id_cartera_credito
                    ) FILTER (
                        WHERE
                            c.riesgo_mejorado_desde_inicial = TRUE
                    ) AS cantidad_mejorados,

                    COUNT(
                        DISTINCT c.id_cartera_credito
                    ) FILTER (
                        WHERE
                            c.codigo_modificacion_credito = '2'
                    ) AS cantidad_reestructurados,

                    COUNT(
                        DISTINCT c.id_cartera_credito
                    ) FILTER (
                        WHERE
                            c.codigo_estado_juridico
                                IN ('1', '2', '3')
                    ) AS cantidad_juridicos

                FROM creditos_filtrados c
            ),
            resumen_recaudos AS (
                  SELECT
                      COALESCE(
                          SUM(e.total_componentes_registrados),
                          0
                      ) AS recaudo_periodo,
            
                      COALESCE(
                          SUM(e.valor_capital),
                          0
                      ) AS capital_recaudado,
            
                      COALESCE(
                          SUM(
                              COALESCE(
                                  e.valor_interes_causado,
                                  0
                              )
                              +
                              COALESCE(
                                  e.valor_interes_ingreso,
                                  0
                              )
                              +
                              COALESCE(
                                  e.valor_interes_anticipado,
                                  0
                              )
                          ),
                          0
                      ) AS intereses_recaudados,
            
                      COALESCE(
                          SUM(e.valor_interes_mora),
                          0
                      ) AS intereses_mora_recaudados
            
                  FROM %s e
            
                  WHERE 1 = 1
                  %s
              )
            SELECT
                c.saldo_cartera,
                c.saldo_neto_pendiente,
                c.saldo_creditos_mora,

                CASE
                    WHEN c.saldo_cartera = 0
                        THEN 0
                    ELSE
                        ROUND(
                            (
                                c.saldo_creditos_mora
                                / c.saldo_cartera
                            ) * 100,
                            4
                        )
                END AS indice_mora,

                c.cantidad_creditos_con_saldo,
                c.cantidad_asociados,
                c.cantidad_creditos_mora,
                c.cantidad_creditos_criticos,

                r.recaudo_periodo,
                r.capital_recaudado,
                r.intereses_recaudados,
                r.intereses_mora_recaudados,

                c.cantidad_deteriorados,
                c.cantidad_mejorados,
                c.cantidad_reestructurados,
                c.cantidad_juridicos

            FROM resumen_creditos c

            CROSS JOIN resumen_recaudos r
            """.formatted(
                VISTA_CREDITOS,
                construirFiltrosCreditos(filtroSeguro),
                VISTA_EXTRACTOS,
                construirFiltrosExtractos(filtroSeguro)
        );

        List<DashboardCarteraResumenDTO> resultado =
                jdbcTemplate.query(
                        sql,
                        parametros,
                        new BeanPropertyRowMapper<>(
                                DashboardCarteraResumenDTO.class
                        )
                );

        if (resultado.isEmpty()) {
            return new DashboardCarteraResumenDTO();
        }

        return resultado.get(0);
    }

    // =========================================================
    // Distribución por edad de riesgo
    // =========================================================

    public List<DashboardCarteraRiesgoDTO> consultarRiesgos(
            DashboardCarteraRequestDTO filtro
    ) {

        DashboardCarteraRequestDTO filtroSeguro =
                normalizarFiltro(filtro);

        String sql = """
                WITH datos AS (
                    SELECT
                        COALESCE(
                            NULLIF(
                                TRIM(c.edad_de_riesgo),
                                ''
                            ),
                            'SIN_DATO'
                        ) AS codigo,

                        COALESCE(
                            NULLIF(
                                TRIM(c.descripcion_edad_de_riesgo),
                                ''
                            ),
                            'Sin clasificación de riesgo'
                        ) AS descripcion,

                        COALESCE(
                            c.orden_edad_riesgo,
                            999
                        ) AS orden,

                        c.id_cartera_credito,
                        c.id_datos_personal,

                        COALESCE(
                            c.saldo_actual,
                            0
                        ) AS saldo_actual,

                        COALESCE(
                            c.credito_en_mora,
                            FALSE
                        ) AS credito_en_mora

                    FROM %s c

                    WHERE 1 = 1
                    %s
                ),
                totales AS (
                    SELECT
                        COALESCE(
                            SUM(d.saldo_actual),
                            0
                        ) AS saldo_total

                    FROM datos d
                )
                SELECT
                    d.codigo,
                    d.descripcion,
                    d.orden,

                    COUNT(
                        DISTINCT d.id_cartera_credito
                    ) AS cantidad_creditos,

                    COUNT(
                        DISTINCT d.id_datos_personal
                    ) AS cantidad_asociados,

                    COALESCE(
                        SUM(d.saldo_actual),
                        0
                    ) AS saldo_cartera,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN d.credito_en_mora = TRUE
                                    THEN d.saldo_actual
                                ELSE 0
                            END
                        ),
                        0
                    ) AS saldo_creditos_mora,

                    CASE
                        WHEN t.saldo_total = 0
                            THEN 0
                        ELSE
                            ROUND(
                                (
                                    SUM(d.saldo_actual)
                                    / t.saldo_total
                                ) * 100,
                                4
                            )
                    END AS porcentaje_participacion

                FROM datos d

                CROSS JOIN totales t

                GROUP BY
                    d.codigo,
                    d.descripcion,
                    d.orden,
                    t.saldo_total

                ORDER BY
                    d.orden,
                    d.codigo
                """.formatted(
                VISTA_CREDITOS,
                construirFiltrosCreditos(filtroSeguro)
        );

        return jdbcTemplate.query(
                sql,
                construirParametros(filtroSeguro),
                new BeanPropertyRowMapper<>(
                        DashboardCarteraRiesgoDTO.class
                )
        );
    }

    // =========================================================
    // Distribución por edad de mora
    // =========================================================

    public List<DashboardCarteraRiesgoDTO> consultarMoras(
            DashboardCarteraRequestDTO filtro
    ) {

        DashboardCarteraRequestDTO filtroSeguro =
                normalizarFiltro(filtro);

        String sql = """
                WITH datos AS (
                    SELECT
                        COALESCE(
                            NULLIF(
                                TRIM(c.edad_de_mora),
                                ''
                            ),
                            'SIN_DATO'
                        ) AS codigo,

                        COALESCE(
                            NULLIF(
                                TRIM(c.descripcion_edad_de_mora),
                                ''
                            ),
                            'Sin clasificación de mora'
                        ) AS descripcion,

                        COALESCE(
                            c.orden_edad_mora,
                            999
                        ) AS orden,

                        c.id_cartera_credito,
                        c.id_datos_personal,

                        COALESCE(
                            c.saldo_actual,
                            0
                        ) AS saldo_actual,

                        COALESCE(
                            c.credito_en_mora,
                            FALSE
                        ) AS credito_en_mora

                    FROM %s c

                    WHERE 1 = 1
                    %s
                ),
                totales AS (
                    SELECT
                        COALESCE(
                            SUM(d.saldo_actual),
                            0
                        ) AS saldo_total

                    FROM datos d
                )
                SELECT
                    d.codigo,
                    d.descripcion,
                    d.orden,

                    COUNT(
                        DISTINCT d.id_cartera_credito
                    ) AS cantidad_creditos,

                    COUNT(
                        DISTINCT d.id_datos_personal
                    ) AS cantidad_asociados,

                    COALESCE(
                        SUM(d.saldo_actual),
                        0
                    ) AS saldo_cartera,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN d.credito_en_mora = TRUE
                                    THEN d.saldo_actual
                                ELSE 0
                            END
                        ),
                        0
                    ) AS saldo_creditos_mora,

                    CASE
                        WHEN t.saldo_total = 0
                            THEN 0
                        ELSE
                            ROUND(
                                (
                                    SUM(d.saldo_actual)
                                    / t.saldo_total
                                ) * 100,
                                4
                            )
                    END AS porcentaje_participacion

                FROM datos d

                CROSS JOIN totales t

                GROUP BY
                    d.codigo,
                    d.descripcion,
                    d.orden,
                    t.saldo_total

                ORDER BY
                    d.orden,
                    d.codigo
                """.formatted(
                VISTA_CREDITOS,
                construirFiltrosCreditos(filtroSeguro)
        );

        return jdbcTemplate.query(
                sql,
                construirParametros(filtroSeguro),
                new BeanPropertyRowMapper<>(
                        DashboardCarteraRiesgoDTO.class
                )
        );
    }

    // =========================================================
    // Distribución por línea de crédito
    // =========================================================

    public List<DashboardCarteraLineaDTO> consultarLineas(
            DashboardCarteraRequestDTO filtro
    ) {

        DashboardCarteraRequestDTO filtroSeguro =
                normalizarFiltro(filtro);

        String sql = """
                WITH datos AS (
                    SELECT
                        c.id_linea_credito,
                        c.codigo_linea_credito,
                        c.nombre_linea_credito,
                        c.id_cartera_credito,
                        c.id_datos_personal,

                        COALESCE(
                            c.valor_desembolsado,
                            0
                        ) AS valor_desembolsado,

                        COALESCE(
                            c.saldo_actual,
                            0
                        ) AS saldo_actual,

                        COALESCE(
                            c.saldo_neto_pendiente,
                            0
                        ) AS saldo_neto_pendiente,

                        COALESCE(
                            c.credito_en_mora,
                            FALSE
                        ) AS credito_en_mora

                    FROM %s c

                    WHERE 1 = 1
                    %s
                ),
                totales AS (
                    SELECT
                        COALESCE(
                            SUM(d.saldo_actual),
                            0
                        ) AS saldo_total

                    FROM datos d
                )
                SELECT
                    d.id_linea_credito,
                    d.codigo_linea_credito,
                    d.nombre_linea_credito,

                    COUNT(
                        DISTINCT d.id_cartera_credito
                    ) AS cantidad_creditos,

                    COUNT(
                        DISTINCT d.id_datos_personal
                    ) AS cantidad_asociados,

                    COALESCE(
                        SUM(d.valor_desembolsado),
                        0
                    ) AS valor_desembolsado,

                    COALESCE(
                        SUM(d.saldo_actual),
                        0
                    ) AS saldo_cartera,

                    COALESCE(
                        SUM(d.saldo_neto_pendiente),
                        0
                    ) AS saldo_neto_pendiente,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN d.credito_en_mora = TRUE
                                    THEN d.saldo_actual
                                ELSE 0
                            END
                        ),
                        0
                    ) AS saldo_creditos_mora,

                    CASE
                        WHEN COALESCE(
                            SUM(d.saldo_actual),
                            0
                        ) = 0
                            THEN 0
                        ELSE
                            ROUND(
                                (
                                    SUM(
                                        CASE
                                            WHEN d.credito_en_mora = TRUE
                                                THEN d.saldo_actual
                                            ELSE 0
                                        END
                                    )
                                    / SUM(d.saldo_actual)
                                ) * 100,
                                4
                            )
                    END AS indice_mora,

                    CASE
                        WHEN t.saldo_total = 0
                            THEN 0
                        ELSE
                            ROUND(
                                (
                                    SUM(d.saldo_actual)
                                    / t.saldo_total
                                ) * 100,
                                4
                            )
                    END AS porcentaje_participacion

                FROM datos d

                CROSS JOIN totales t

                GROUP BY
                    d.id_linea_credito,
                    d.codigo_linea_credito,
                    d.nombre_linea_credito,
                    t.saldo_total

                ORDER BY
                    saldo_cartera DESC,
                    d.codigo_linea_credito
                """.formatted(
                VISTA_CREDITOS,
                construirFiltrosCreditos(filtroSeguro)
        );

        return jdbcTemplate.query(
                sql,
                construirParametros(filtroSeguro),
                new BeanPropertyRowMapper<>(
                        DashboardCarteraLineaDTO.class
                )
        );
    }

    // =========================================================
    // Distribución por agencia
    // =========================================================

    public List<DashboardCarteraAgenciaDTO> consultarAgencias(
            DashboardCarteraRequestDTO filtro
    ) {

        DashboardCarteraRequestDTO filtroSeguro =
                normalizarFiltro(filtro);

        String sql = """
                WITH creditos AS (
                    SELECT
                        c.id_agencia,
                        c.codigo_agencia,
                        c.nombre_agencia,
                        c.id_cartera_credito,
                        c.id_datos_personal,

                        COALESCE(
                            c.valor_desembolsado,
                            0
                        ) AS valor_desembolsado,

                        COALESCE(
                            c.saldo_actual,
                            0
                        ) AS saldo_actual,

                        COALESCE(
                            c.saldo_neto_pendiente,
                            0
                        ) AS saldo_neto_pendiente,

                        COALESCE(
                            c.credito_en_mora,
                            FALSE
                        ) AS credito_en_mora

                    FROM %s c

                    WHERE 1 = 1
                    %s
                ),
                recaudos AS (
                    SELECT
                        e.id_agencia,

                        COALESCE(
                            SUM(e.total_componentes_registrados),
                            0
                        ) AS recaudo_periodo

                    FROM %s e

                    WHERE 1 = 1
                    %s

                    GROUP BY
                        e.id_agencia
                ),
                totales AS (
                    SELECT
                        COALESCE(
                            SUM(c.saldo_actual),
                            0
                        ) AS saldo_total

                    FROM creditos c
                )
                SELECT
                    c.id_agencia,
                    c.codigo_agencia,
                    c.nombre_agencia,

                    COUNT(
                        DISTINCT c.id_cartera_credito
                    ) AS cantidad_creditos,

                    COUNT(
                        DISTINCT c.id_datos_personal
                    ) AS cantidad_asociados,

                    COALESCE(
                        SUM(c.valor_desembolsado),
                        0
                    ) AS valor_desembolsado,

                    COALESCE(
                        SUM(c.saldo_actual),
                        0
                    ) AS saldo_cartera,

                    COALESCE(
                        SUM(c.saldo_neto_pendiente),
                        0
                    ) AS saldo_neto_pendiente,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN c.credito_en_mora = TRUE
                                    THEN c.saldo_actual
                                ELSE 0
                            END
                        ),
                        0
                    ) AS saldo_creditos_mora,

                    COALESCE(
                        MAX(r.recaudo_periodo),
                        0
                    ) AS recaudo_periodo,

                    CASE
                        WHEN COALESCE(
                            SUM(c.saldo_actual),
                            0
                        ) = 0
                            THEN 0
                        ELSE
                            ROUND(
                                (
                                    SUM(
                                        CASE
                                            WHEN c.credito_en_mora = TRUE
                                                THEN c.saldo_actual
                                            ELSE 0
                                        END
                                    )
                                    / SUM(c.saldo_actual)
                                ) * 100,
                                4
                            )
                    END AS indice_mora,

                    CASE
                        WHEN t.saldo_total = 0
                            THEN 0
                        ELSE
                            ROUND(
                                (
                                    SUM(c.saldo_actual)
                                    / t.saldo_total
                                ) * 100,
                                4
                            )
                    END AS porcentaje_participacion

                FROM creditos c

                CROSS JOIN totales t

                LEFT JOIN recaudos r
                    ON r.id_agencia = c.id_agencia

                GROUP BY
                    c.id_agencia,
                    c.codigo_agencia,
                    c.nombre_agencia,
                    t.saldo_total

                ORDER BY
                    saldo_cartera DESC,
                    c.nombre_agencia
                """.formatted(
                VISTA_CREDITOS,
                construirFiltrosCreditos(filtroSeguro),
                VISTA_EXTRACTOS,
                construirFiltrosExtractos(filtroSeguro)
        );

        return jdbcTemplate.query(
                sql,
                construirParametros(filtroSeguro),
                new BeanPropertyRowMapper<>(
                        DashboardCarteraAgenciaDTO.class
                )
        );
    }

    // =========================================================
    // Composición del recaudo
    // =========================================================

    public List<DashboardCarteraRecaudoDTO> consultarRecaudos(
            DashboardCarteraRequestDTO filtro
    ) {

        DashboardCarteraRequestDTO filtroSeguro =
                normalizarFiltro(filtro);

        String sql = """
            WITH resumen_recaudo AS (
                SELECT
                    COUNT(*) FILTER (
                        WHERE COALESCE(
                            e.valor_capital,
                            0
                        ) <> 0
                    ) AS cantidad_capital,

                    COALESCE(
                        SUM(e.valor_capital),
                        0
                    ) AS valor_capital,

                    COUNT(*) FILTER (
                        WHERE (
                            COALESCE(
                                e.valor_interes_causado,
                                0
                            )
                            +
                            COALESCE(
                                e.valor_interes_ingreso,
                                0
                            )
                            +
                            COALESCE(
                                e.valor_interes_anticipado,
                                0
                            )
                        ) <> 0
                    ) AS cantidad_intereses_corrientes,

                    COALESCE(
                        SUM(
                            COALESCE(
                                e.valor_interes_causado,
                                0
                            )
                            +
                            COALESCE(
                                e.valor_interes_ingreso,
                                0
                            )
                            +
                            COALESCE(
                                e.valor_interes_anticipado,
                                0
                            )
                        ),
                        0
                    ) AS valor_intereses_corrientes,

                    COUNT(*) FILTER (
                        WHERE COALESCE(
                            e.valor_interes_mora,
                            0
                        ) <> 0
                    ) AS cantidad_intereses_mora,

                    COALESCE(
                        SUM(e.valor_interes_mora),
                        0
                    ) AS valor_intereses_mora,

                    COUNT(*) FILTER (
                        WHERE COALESCE(
                            e.valor_seguro,
                            0
                        ) <> 0
                    ) AS cantidad_seguros,

                    COALESCE(
                        SUM(e.valor_seguro),
                        0
                    ) AS valor_seguros,

                    COUNT(*) FILTER (
                        WHERE (
                            COALESCE(
                                e.valor_aportes,
                                0
                            )
                            +
                            COALESCE(
                                e.valor_papeleria,
                                0
                            )
                            +
                            COALESCE(
                                e.valor_fondo_garantia,
                                0
                            )
                            +
                            COALESCE(
                                e.otros_valores,
                                0
                            )
                        ) <> 0
                    ) AS cantidad_otros,

                    COALESCE(
                        SUM(
                            COALESCE(
                                e.valor_aportes,
                                0
                            )
                            +
                            COALESCE(
                                e.valor_papeleria,
                                0
                            )
                            +
                            COALESCE(
                                e.valor_fondo_garantia,
                                0
                            )
                            +
                            COALESCE(
                                e.otros_valores,
                                0
                            )
                        ),
                        0
                    ) AS valor_otros

                FROM %s e

                WHERE 1 = 1
                %s
            ),
            conceptos AS (
                SELECT
                    'CAPITAL' AS codigo,
                    'Capital' AS descripcion,
                    1 AS orden,
                    r.cantidad_capital AS cantidad_movimientos,
                    r.valor_capital AS valor_recaudado

                FROM resumen_recaudo r

                UNION ALL

                SELECT
                    'INTERESES_CORRIENTES',
                    'Intereses corrientes',
                    2,
                    r.cantidad_intereses_corrientes,
                    r.valor_intereses_corrientes

                FROM resumen_recaudo r

                UNION ALL

                SELECT
                    'INTERESES_MORA',
                    'Intereses de mora',
                    3,
                    r.cantidad_intereses_mora,
                    r.valor_intereses_mora

                FROM resumen_recaudo r

                UNION ALL

                SELECT
                    'SEGUROS',
                    'Seguros',
                    4,
                    r.cantidad_seguros,
                    r.valor_seguros

                FROM resumen_recaudo r

                UNION ALL

                SELECT
                    'OTROS',
                    'Otros conceptos',
                    5,
                    r.cantidad_otros,
                    r.valor_otros

                FROM resumen_recaudo r
            ),
            totales AS (
                SELECT
                    COALESCE(
                        SUM(c.valor_recaudado),
                        0
                    ) AS valor_total

                FROM conceptos c
            )
            SELECT
                c.codigo,
                c.descripcion,
                c.orden,
                c.cantidad_movimientos,
                c.valor_recaudado,

                CASE
                    WHEN t.valor_total = 0
                        THEN 0
                    ELSE
                        ROUND(
                            (
                                c.valor_recaudado
                                / t.valor_total
                            ) * 100,
                            4
                        )
                END AS porcentaje_participacion

            FROM conceptos c

            CROSS JOIN totales t

            WHERE
                c.valor_recaudado <> 0

            ORDER BY
                c.orden
            """.formatted(
                VISTA_EXTRACTOS,
                construirFiltrosExtractos(filtroSeguro)
        );

        return jdbcTemplate.query(
                sql,
                construirParametros(filtroSeguro),
                new BeanPropertyRowMapper<>(
                        DashboardCarteraRecaudoDTO.class
                )
        );
    }

    // =========================================================
    // Alertas gerenciales
    // =========================================================

    public List<DashboardCarteraAlertaDTO> consultarAlertas(
            DashboardCarteraRequestDTO filtro
    ) {

        DashboardCarteraRequestDTO filtroSeguro =
                normalizarFiltro(filtro);

        String sql = """
                WITH datos AS (
                    SELECT
                        c.id_cartera_credito,
                        c.id_datos_personal,

                        COALESCE(
                            c.saldo_actual,
                            0
                        ) AS saldo_actual,

                        COALESCE(
                            c.credito_en_mora,
                            FALSE
                        ) AS credito_en_mora,

                        COALESCE(
                            c.riesgo_alto,
                            FALSE
                        ) AS riesgo_alto,

                        COALESCE(
                            c.mora_alta,
                            FALSE
                        ) AS mora_alta,

                        COALESCE(
                            c.riesgo_deteriorado_desde_inicial,
                            FALSE
                        ) AS riesgo_deteriorado_desde_inicial,

                        c.codigo_estado_juridico,
                        c.codigo_modificacion_credito,
                        c.fecha_final

                    FROM %s c

                    WHERE 1 = 1
                    %s
                ),
                alertas AS (
                    SELECT
                        'CRITICOS' AS codigo,
                        'Créditos críticos' AS descripcion,
                        'CRITICO' AS nivel,
                        1 AS orden,

                        COUNT(
                            DISTINCT d.id_cartera_credito
                        ) AS cantidad_creditos,

                        COUNT(
                            DISTINCT d.id_datos_personal
                        ) AS cantidad_asociados,

                        COALESCE(
                            SUM(d.saldo_actual),
                            0
                        ) AS saldo_cartera,

                        COALESCE(
                            SUM(
                                CASE
                                    WHEN d.credito_en_mora = TRUE
                                        THEN d.saldo_actual
                                    ELSE 0
                                END
                            ),
                            0
                        ) AS saldo_creditos_mora

                    FROM datos d

                    WHERE
                        d.riesgo_alto = TRUE
                        OR d.mora_alta = TRUE

                    UNION ALL

                    SELECT
                        'JURIDICOS',
                        'Créditos con gestión jurídica',
                        'CRITICO',
                        2,

                        COUNT(
                            DISTINCT d.id_cartera_credito
                        ),

                        COUNT(
                            DISTINCT d.id_datos_personal
                        ),

                        COALESCE(
                            SUM(d.saldo_actual),
                            0
                        ),

                        COALESCE(
                            SUM(
                                CASE
                                    WHEN d.credito_en_mora = TRUE
                                        THEN d.saldo_actual
                                    ELSE 0
                                END
                            ),
                            0
                        )

                    FROM datos d

                    WHERE
                        d.codigo_estado_juridico
                            IN ('1', '2', '3')

                    UNION ALL

                    SELECT
                        'DETERIORADOS',
                        'Créditos deteriorados',
                        'ADVERTENCIA',
                        3,

                        COUNT(
                            DISTINCT d.id_cartera_credito
                        ),

                        COUNT(
                            DISTINCT d.id_datos_personal
                        ),

                        COALESCE(
                            SUM(d.saldo_actual),
                            0
                        ),

                        COALESCE(
                            SUM(
                                CASE
                                    WHEN d.credito_en_mora = TRUE
                                        THEN d.saldo_actual
                                    ELSE 0
                                END
                            ),
                            0
                        )

                    FROM datos d

                    WHERE
                        d.riesgo_deteriorado_desde_inicial = TRUE

                    UNION ALL

                    SELECT
                        'REESTRUCTURADOS',
                        'Créditos reestructurados',
                        'ADVERTENCIA',
                        4,

                        COUNT(
                            DISTINCT d.id_cartera_credito
                        ),

                        COUNT(
                            DISTINCT d.id_datos_personal
                        ),

                        COALESCE(
                            SUM(d.saldo_actual),
                            0
                        ),

                        COALESCE(
                            SUM(
                                CASE
                                    WHEN d.credito_en_mora = TRUE
                                        THEN d.saldo_actual
                                    ELSE 0
                                END
                            ),
                            0
                        )

                    FROM datos d

                    WHERE
                        d.codigo_modificacion_credito = '2'

                    UNION ALL

                    SELECT
                        'NOVADOS',
                        'Créditos novados',
                        'INFORMACION',
                        5,

                        COUNT(
                            DISTINCT d.id_cartera_credito
                        ),

                        COUNT(
                            DISTINCT d.id_datos_personal
                        ),

                        COALESCE(
                            SUM(d.saldo_actual),
                            0
                        ),

                        COALESCE(
                            SUM(
                                CASE
                                    WHEN d.credito_en_mora = TRUE
                                        THEN d.saldo_actual
                                    ELSE 0
                                END
                            ),
                            0
                        )

                    FROM datos d

                    WHERE
                        d.codigo_modificacion_credito = '3'

                    UNION ALL

                    SELECT
                        'PROXIMOS_VENCER',
                        'Créditos próximos a vencer',
                        'INFORMACION',
                        6,

                        COUNT(
                            DISTINCT d.id_cartera_credito
                        ),

                        COUNT(
                            DISTINCT d.id_datos_personal
                        ),

                        COALESCE(
                            SUM(d.saldo_actual),
                            0
                        ),

                        COALESCE(
                            SUM(
                                CASE
                                    WHEN d.credito_en_mora = TRUE
                                        THEN d.saldo_actual
                                    ELSE 0
                                END
                            ),
                            0
                        )

                    FROM datos d

                    WHERE
                        d.fecha_final BETWEEN
                            :fechaCorte
                            AND (
                                :fechaCorte
                                + INTERVAL '30 days'
                            )
                )
                SELECT
                    a.codigo,
                    a.descripcion,
                    a.nivel,
                    a.orden,
                    a.cantidad_creditos,
                    a.cantidad_asociados,
                    a.saldo_cartera,
                    a.saldo_creditos_mora

                FROM alertas a

                ORDER BY
                    a.orden
                """.formatted(
                VISTA_CREDITOS,
                construirFiltrosCreditos(filtroSeguro)
        );

        return jdbcTemplate.query(
                sql,
                construirParametros(filtroSeguro),
                new BeanPropertyRowMapper<>(
                        DashboardCarteraAlertaDTO.class
                )
        );
    }

    // =========================================================
    // Parámetros
    // =========================================================

    private MapSqlParameterSource construirParametros(
            DashboardCarteraRequestDTO filtro
    ) {

        LocalDate fechaCorte =
                filtro.getFechaCorte() != null
                        ? filtro.getFechaCorte()
                        : LocalDate.now();

        LocalDate fechaDesde =
                filtro.getFechaDesde() != null
                        ? filtro.getFechaDesde()
                        : fechaCorte.withDayOfMonth(1);

        LocalDate fechaHasta =
                filtro.getFechaHasta() != null
                        ? filtro.getFechaHasta()
                        : fechaCorte;

        return new MapSqlParameterSource()
                .addValue(
                        "fechaCorte",
                        fechaCorte
                )
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
                        filtro.getIdAgencia()
                )
                .addValue(
                        "idLineaCredito",
                        filtro.getIdLineaCredito()
                )
                .addValue(
                        "edadRiesgo",
                        filtro.getEdadRiesgo()
                )
                .addValue(
                        "edadMora",
                        filtro.getEdadMora()
                )
                .addValue(
                        "codigoEstadoCartera",
                        filtro.getCodigoEstadoCartera()
                )
                .addValue(
                        "codigoEstadoJuridico",
                        filtro.getCodigoEstadoJuridico()
                )
                .addValue(
                        "codigoClasificacionCredito",
                        filtro.getCodigoClasificacionCredito()
                )
                .addValue(
                        "codigoGarantiaCredito",
                        filtro.getCodigoGarantiaCredito()
                );
    }

    // =========================================================
    // Filtros para cartera
    // =========================================================

    private String construirFiltrosCreditos(
            DashboardCarteraRequestDTO filtro
    ) {

        StringBuilder sql =
                new StringBuilder();

        sql.append("""
                
                AND c.fecha_desembolso <= :fechaCorte
                AND COALESCE(c.saldo_actual, 0) > 0
                """);

        if (filtro.getIdAgencia() != null) {
            sql.append("""
                    
                    AND c.id_agencia = :idAgencia
                    """);
        }

        if (filtro.getIdLineaCredito() != null) {
            sql.append("""
                    
                    AND c.id_linea_credito = :idLineaCredito
                    """);
        }

        if (tieneTexto(filtro.getEdadRiesgo())) {
            sql.append("""
                    
                    AND UPPER(
                        TRIM(COALESCE(c.edad_de_riesgo, ''))
                    ) = UPPER(
                        TRIM(:edadRiesgo)
                    )
                    """);
        }

        if (tieneTexto(filtro.getEdadMora())) {
            sql.append("""
                    
                    AND UPPER(
                        TRIM(COALESCE(c.edad_de_mora, ''))
                    ) = UPPER(
                        TRIM(:edadMora)
                    )
                    """);
        }

        if (tieneTexto(
                filtro.getCodigoEstadoCartera()
        )) {
            sql.append("""
                    
                    AND UPPER(
                        TRIM(COALESCE(c.codigo_estado_cartera, ''))
                    ) = UPPER(
                        TRIM(:codigoEstadoCartera)
                    )
                    """);
        }

        if (tieneTexto(
                filtro.getCodigoEstadoJuridico()
        )) {
            sql.append("""
                    
                    AND UPPER(
                        TRIM(COALESCE(c.codigo_estado_juridico, ''))
                    ) = UPPER(
                        TRIM(:codigoEstadoJuridico)
                    )
                    """);
        }

        if (tieneTexto(
                filtro.getCodigoClasificacionCredito()
        )) {
            sql.append("""
                    
                    AND UPPER(
                        TRIM(
                            COALESCE(
                                c.codigo_clasificacion_credito,
                                ''
                            )
                        )
                    ) = UPPER(
                        TRIM(:codigoClasificacionCredito)
                    )
                    """);
        }

        if (tieneTexto(
                filtro.getCodigoGarantiaCredito()
        )) {
            sql.append("""
                    
                    AND UPPER(
                        TRIM(
                            COALESCE(
                                c.codigo_garantia_credito,
                                ''
                            )
                        )
                    ) = UPPER(
                        TRIM(:codigoGarantiaCredito)
                    )
                    """);
        }

        return sql.toString();
    }

    // =========================================================
    // Filtros para extractos
    // =========================================================

    private String construirFiltrosExtractos(
            DashboardCarteraRequestDTO filtro
    ) {

        StringBuilder sql =
                new StringBuilder();

        sql.append("""
            
            AND e.fecha_contable BETWEEN
                :fechaDesde
                AND :fechaHasta

            AND e.movimiento_activo = TRUE
            """);

        if (filtro.getIdAgencia() != null) {
            sql.append("""
                    
                    AND e.id_agencia = :idAgencia
                    """);
        }

        if (filtro.getIdLineaCredito() != null) {
            sql.append("""
                    
                    AND e.id_linea_credito = :idLineaCredito
                    """);
        }

        return sql.toString();
    }

    // =========================================================
    // Utilidades
    // =========================================================

    private DashboardCarteraRequestDTO normalizarFiltro(
            DashboardCarteraRequestDTO filtro
    ) {

        if (filtro != null) {
            return filtro;
        }

        return new DashboardCarteraRequestDTO();
    }

    private boolean tieneTexto(String valor) {
        return valor != null
                && !valor.trim().isEmpty();
    }
}