package co.assip.erp.cartera.calculosprevios;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CalculosPreviosRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // CALCULAR DÍAS DE MORA Y EDAD DE MORA
    //
    // REGLAS:
    //
    // dias_mora =
    //     MAX(
    //         fecha_corte - proxima_fecha_capital,
    //         0
    //     )
    //
    // edad_de_mora:
    //     se obtiene de cartera.clasificaciones_mora
    //     según:
    //
    //     - codigo_clasificacion_credito
    //     - dias_mora
    //     - vigencia a fecha de corte
    //     - activo = true
    //
    // Este proceso trabaja únicamente sobre los créditos
    // fotografiados del cierre.
    // =========================================================

    public int calcularMora(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
            WITH base AS
            (
                SELECT
                    r.id_cierre_cartera_resultado,

                    CASE

                        WHEN f.proxima_fecha_capital IS NULL
                        THEN 0

                        WHEN f.proxima_fecha_capital > c.fecha_corte
                        THEN 0

                        ELSE GREATEST(
                            (
                                (
                                    EXTRACT(
                                        YEAR
                                        FROM c.fecha_corte
                                    )::integer
                                    -
                                    EXTRACT(
                                        YEAR
                                        FROM f.proxima_fecha_capital
                                    )::integer
                                ) * 360
                            )
                            +
                            (
                                (
                                    EXTRACT(
                                        MONTH
                                        FROM c.fecha_corte
                                    )::integer
                                    -
                                    EXTRACT(
                                        MONTH
                                        FROM f.proxima_fecha_capital
                                    )::integer
                                ) * 30
                            )
                            +
                            (
                                LEAST(
                                    EXTRACT(
                                        DAY
                                        FROM c.fecha_corte
                                    )::integer,
                                    30
                                )
                                -
                                LEAST(
                                    EXTRACT(
                                        DAY
                                        FROM f.proxima_fecha_capital
                                    )::integer,
                                    30
                                )
                            ),
                            0
                        )

                    END AS dias_mora,

                    f.codigo_clasificacion_credito,

                    c.fecha_corte

                FROM cartera.cierres_cartera_resultados r

                INNER JOIN cartera.cierres_cartera_creditos f
                    ON f.id_cierre_cartera_credito =
                       r.id_cierre_cartera_credito

                INNER JOIN cartera.cierres_cartera c
                    ON c.id_cierre_cartera =
                       r.id_cierre_cartera

                WHERE r.id_cierre_cartera =
                      :idCierreCartera
            ),

            clasificada AS
            (
                SELECT
                    b.id_cierre_cartera_resultado,
                    b.dias_mora,
                    cm.codigo_edad_mora

                FROM base b

                LEFT JOIN cartera.clasificaciones_mora cm
                    ON cm.codigo_clasificacion_credito =
                       b.codigo_clasificacion_credito

                   AND b.dias_mora
                       BETWEEN cm.dias_desde
                           AND cm.dias_hasta

                   AND cm.activo = true

                   AND cm.vigencia_desde
                       <= b.fecha_corte

                   AND (
                        cm.vigencia_hasta IS NULL
                        OR cm.vigencia_hasta
                           >= b.fecha_corte
                   )
            )

            UPDATE cartera.cierres_cartera_resultados r

               SET dias_mora =
                       c.dias_mora,

                   edad_de_mora =
                       c.codigo_edad_mora,

                   fk_seguridad_edicion =
                       :idUsuario,

                   fecha_edicion =
                       CURRENT_TIMESTAMP

              FROM clasificada c

             WHERE r.id_cierre_cartera_resultado =
                   c.id_cierre_cartera_resultado
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // CONTAR RESULTADOS DEL CIERRE
    // =========================================================

    public int contarResultados(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)

                FROM cartera.cierres_cartera_resultados r

                WHERE r.id_cierre_cartera =
                      :idCierreCartera
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }

    // =========================================================
    // CONTAR REGISTROS SIN EDAD DE MORA
    //
    // Después de calcular, debe dar 0.
    // =========================================================

    public int contarSinEdadMora(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)

                FROM cartera.cierres_cartera_resultados r

                WHERE r.id_cierre_cartera =
                      :idCierreCartera

                  AND (
                       r.edad_de_mora IS NULL
                       OR TRIM(r.edad_de_mora) = ''
                  )
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }

    // =========================================================
    // CONTAR REGISTROS CON DÍAS DE MORA NEGATIVOS
    //
    // Debe dar siempre 0.
    // =========================================================

    public int contarDiasMoraNegativos(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)

                FROM cartera.cierres_cartera_resultados r

                WHERE r.id_cierre_cartera =
                      :idCierreCartera

                  AND r.dias_mora < 0
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }

    // =========================================================
// CALCULAR BANDERAS COMUNES
//
// es_una_sola_cuota:
//     amortizacion_capital = plazo
//
// es_reestructurado:
//     se toma directamente de la fotografía del crédito
//     mediante credito_reestructurado.
// =========================================================

    public int calcularBanderas(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cartera.cierres_cartera_resultados r

               SET es_una_sola_cuota =
                       (
                           f.amortizacion_capital =
                           f.plazo
                       ),

                   es_reestructurado =
                       COALESCE(
                           f.credito_reestructurado,
                           false
                       ),

                   fk_seguridad_edicion =
                       :idUsuario,

                   fecha_edicion =
                       CURRENT_TIMESTAMP

              FROM cartera.cierres_cartera_creditos f

             WHERE r.id_cierre_cartera_credito =
                   f.id_cierre_cartera_credito

               AND r.id_cierre_cartera =
                   :idCierreCartera
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
// CONTAR CRÉDITOS DE UNA SOLA CUOTA
// =========================================================

    public int contarUnaSolaCuota(
            Integer idCierreCartera
    ) {

        String sql = """
            SELECT COUNT(*)

            FROM cartera.cierres_cartera_resultados r

            WHERE r.id_cierre_cartera =
                  :idCierreCartera

              AND r.es_una_sola_cuota = true
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }


// =========================================================
// CONTAR CRÉDITOS REESTRUCTURADOS
// =========================================================

    public int contarReestructurados(
            Integer idCierreCartera
    ) {

        String sql = """
            SELECT COUNT(*)

            FROM cartera.cierres_cartera_resultados r

            WHERE r.id_cierre_cartera =
                  :idCierreCartera

              AND r.es_reestructurado = true
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }

    // =========================================================
// CALCULAR EDADES DE RIESGO
//
// REGLAS:
//
// 1. Se normalizan las edades históricas:
//        F -> E
//
// 2. edad_riesgo_inicial:
//        se toma de la fotografía y se normaliza.
//
// 3. edad_de_riesgo:
//        corresponde a la peor edad entre:
//
//        - edad_riesgo_inicial
//        - edad_de_riesgo de la fotografía
//        - edad_de_mora calculada
//
// ORDEN:
//        A = 1
//        B = 2
//        C = 3
//        D = 4
//        E = 5
//
// Ninguna edad nueva queda en F.
// =========================================================

    public int calcularEdadesRiesgo(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
            WITH base AS
            (
                SELECT
                    r.id_cierre_cartera_resultado,

                    CASE
                        WHEN UPPER(
                            TRIM(
                                COALESCE(
                                    f.edad_riesgo_inicial,
                                    'A'
                                )
                            )
                        ) = 'F'
                            THEN 'E'

                        WHEN UPPER(
                            TRIM(
                                COALESCE(
                                    f.edad_riesgo_inicial,
                                    'A'
                                )
                            )
                        ) IN ('A', 'B', 'C', 'D', 'E')
                            THEN UPPER(
                                TRIM(
                                    COALESCE(
                                        f.edad_riesgo_inicial,
                                        'A'
                                    )
                                )
                            )

                        ELSE 'A'
                    END AS edad_inicial,

                    CASE
                        WHEN UPPER(
                            TRIM(
                                COALESCE(
                                    f.edad_de_riesgo,
                                    'A'
                                )
                            )
                        ) = 'F'
                            THEN 'E'

                        WHEN UPPER(
                            TRIM(
                                COALESCE(
                                    f.edad_de_riesgo,
                                    'A'
                                )
                            )
                        ) IN ('A', 'B', 'C', 'D', 'E')
                            THEN UPPER(
                                TRIM(
                                    COALESCE(
                                        f.edad_de_riesgo,
                                        'A'
                                    )
                                )
                            )

                        ELSE 'A'
                    END AS edad_riesgo_foto,

                    CASE
                        WHEN UPPER(
                            TRIM(
                                COALESCE(
                                    r.edad_de_mora,
                                    'A'
                                )
                            )
                        ) = 'F'
                            THEN 'E'

                        WHEN UPPER(
                            TRIM(
                                COALESCE(
                                    r.edad_de_mora,
                                    'A'
                                )
                            )
                        ) IN ('A', 'B', 'C', 'D', 'E')
                            THEN UPPER(
                                TRIM(
                                    COALESCE(
                                        r.edad_de_mora,
                                        'A'
                                    )
                                )
                            )

                        ELSE 'A'
                    END AS edad_mora

                FROM cartera.cierres_cartera_resultados r

                INNER JOIN cartera.cierres_cartera_creditos f
                    ON f.id_cierre_cartera_credito =
                       r.id_cierre_cartera_credito

                WHERE r.id_cierre_cartera =
                      :idCierreCartera
            ),

            ordenada AS
            (
                SELECT
                    b.id_cierre_cartera_resultado,
                    b.edad_inicial,

                    GREATEST(
                        CASE b.edad_inicial
                            WHEN 'A' THEN 1
                            WHEN 'B' THEN 2
                            WHEN 'C' THEN 3
                            WHEN 'D' THEN 4
                            WHEN 'E' THEN 5
                        END,

                        CASE b.edad_riesgo_foto
                            WHEN 'A' THEN 1
                            WHEN 'B' THEN 2
                            WHEN 'C' THEN 3
                            WHEN 'D' THEN 4
                            WHEN 'E' THEN 5
                        END,

                        CASE b.edad_mora
                            WHEN 'A' THEN 1
                            WHEN 'B' THEN 2
                            WHEN 'C' THEN 3
                            WHEN 'D' THEN 4
                            WHEN 'E' THEN 5
                        END
                    ) AS orden_riesgo

                FROM base b
            ),

            calculada AS
            (
                SELECT
                    o.id_cierre_cartera_resultado,
                    o.edad_inicial,

                    CASE o.orden_riesgo
                        WHEN 1 THEN 'A'
                        WHEN 2 THEN 'B'
                        WHEN 3 THEN 'C'
                        WHEN 4 THEN 'D'
                        WHEN 5 THEN 'E'
                    END AS edad_riesgo

                FROM ordenada o
            )

            UPDATE cartera.cierres_cartera_resultados r

               SET edad_riesgo_inicial =
                       c.edad_inicial,

                   edad_de_riesgo =
                       c.edad_riesgo,

                   fk_seguridad_edicion =
                       :idUsuario,

                   fecha_edicion =
                       CURRENT_TIMESTAMP

              FROM calculada c

             WHERE r.id_cierre_cartera_resultado =
                   c.id_cierre_cartera_resultado
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
// CONTAR EDADES DE RIESGO INVÁLIDAS
//
// Después del cálculo:
// - edad_riesgo_inicial debe ser A-E
// - edad_de_riesgo debe ser A-E
//
// No debe existir NULL, vacío ni F.
// =========================================================

    public int contarEdadesRiesgoInvalidas(
            Integer idCierreCartera
    ) {

        String sql = """
            SELECT COUNT(*)

            FROM cartera.cierres_cartera_resultados r

            WHERE r.id_cierre_cartera =
                  :idCierreCartera

              AND (
                     r.edad_riesgo_inicial IS NULL

                  OR UPPER(
                         TRIM(r.edad_riesgo_inicial)
                     ) NOT IN ('A', 'B', 'C', 'D', 'E')

                  OR r.edad_de_riesgo IS NULL

                  OR UPPER(
                         TRIM(r.edad_de_riesgo)
                     ) NOT IN ('A', 'B', 'C', 'D', 'E')
              )
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }

    // =========================================================
// CALCULAR EDADES DE REESTRUCTURACIÓN
//
// REGLA:
//
// SI NO ES REESTRUCTURADO:
//
// edad_reestructuracion_inicial = edad_riesgo_inicial
// edad_reestructurado           = edad_de_riesgo
//
// SI ES REESTRUCTURADO:
//
// se conserva la pista propia de reestructuración
// congelada en la fotografía.
//
// F se normaliza a E.
//
// La evolución posterior de un crédito efectivamente
// reestructurado se implementará con su regla específica.
// =========================================================

    public int calcularEdadesReestructuracion(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cartera.cierres_cartera_resultados r

               SET edad_reestructuracion_inicial =
                       CASE

                           -- =================================
                           -- NO REESTRUCTURADO
                           -- =================================

                           WHEN COALESCE(
                                    r.es_reestructurado,
                                    false
                                ) = false
                               THEN r.edad_riesgo_inicial

                           -- =================================
                           -- REESTRUCTURADO
                           -- =================================

                           WHEN UPPER(
                                    TRIM(
                                        COALESCE(
                                            f.edad_reestructuracion_inicial,
                                            'A'
                                        )
                                    )
                                ) = 'F'
                               THEN 'E'

                           WHEN UPPER(
                                    TRIM(
                                        COALESCE(
                                            f.edad_reestructuracion_inicial,
                                            'A'
                                        )
                                    )
                                ) IN ('A', 'B', 'C', 'D', 'E')
                               THEN UPPER(
                                    TRIM(
                                        f.edad_reestructuracion_inicial
                                    )
                                )

                           ELSE 'A'

                       END,

                   edad_reestructurado =
                       CASE

                           -- =================================
                           -- NO REESTRUCTURADO
                           -- =================================

                           WHEN COALESCE(
                                    r.es_reestructurado,
                                    false
                                ) = false
                               THEN r.edad_de_riesgo

                           -- =================================
                           -- REESTRUCTURADO
                           -- =================================

                           WHEN UPPER(
                                    TRIM(
                                        COALESCE(
                                            f.edad_reestructurado,
                                            'A'
                                        )
                                    )
                                ) = 'F'
                               THEN 'E'

                           WHEN UPPER(
                                    TRIM(
                                        COALESCE(
                                            f.edad_reestructurado,
                                            'A'
                                        )
                                    )
                                ) IN ('A', 'B', 'C', 'D', 'E')
                               THEN UPPER(
                                    TRIM(
                                        f.edad_reestructurado
                                    )
                                )

                           ELSE 'A'

                       END,

                   fk_seguridad_edicion =
                       :idUsuario,

                   fecha_edicion =
                       CURRENT_TIMESTAMP

              FROM cartera.cierres_cartera_creditos f

             WHERE r.id_cierre_cartera_credito =
                   f.id_cierre_cartera_credito

               AND r.id_cierre_cartera =
                   :idCierreCartera
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // CONTAR EDADES DE REESTRUCTURACIÓN INVÁLIDAS
    //
    // Solamente se permiten:
    // A, B, C, D, E
    //
    // No se permiten:
    // - NULL
    // - vacío
    // - F
    // - cualquier otro valor
    // =========================================================

    public int contarEdadesReestructuracionInvalidas(
            Integer idCierreCartera
    ) {

        String sql = """
            SELECT COUNT(*)

            FROM cartera.cierres_cartera_resultados r

            WHERE r.id_cierre_cartera =
                  :idCierreCartera

              AND (
                     r.edad_reestructuracion_inicial IS NULL

                  OR TRIM(
                         r.edad_reestructuracion_inicial
                     ) = ''

                  OR UPPER(
                         TRIM(
                             r.edad_reestructuracion_inicial
                         )
                     ) NOT IN ('A', 'B', 'C', 'D', 'E')

                  OR r.edad_reestructurado IS NULL

                  OR TRIM(
                         r.edad_reestructurado
                     ) = ''

                  OR UPPER(
                         TRIM(
                             r.edad_reestructurado
                         )
                     ) NOT IN ('A', 'B', 'C', 'D', 'E')
              )
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }


// =========================================================
// CONTAR NO REESTRUCTURADOS INCONSISTENTES
//
// REGLA:
//
// Cuando el crédito NO es reestructurado:
//
// edad_reestructuracion_inicial = edad_riesgo_inicial
// edad_reestructurado           = edad_de_riesgo
//
// Cualquier diferencia constituye una inconsistencia.
// =========================================================

    public int contarNoReestructuradosInconsistentes(
            Integer idCierreCartera
    ) {

        String sql = """
            SELECT COUNT(*)

            FROM cartera.cierres_cartera_resultados r

            WHERE r.id_cierre_cartera =
                  :idCierreCartera

              AND COALESCE(
                      r.es_reestructurado,
                      false
                  ) = false

              AND (
                     r.edad_reestructuracion_inicial
                         IS DISTINCT FROM
                         r.edad_riesgo_inicial

                  OR r.edad_reestructurado
                         IS DISTINCT FROM
                         r.edad_de_riesgo
              )
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }

    // =========================================================
    // CALCULAR PRORRATEO DE APORTES
    //
    // REGLA:
    //
    // 1. Se toman únicamente los créditos activos de la
    //    fotografía del cierre.
    //
    // 2. Por asociado se calcula:
    //
    //    saldo_total_creditos_asociado =
    //        SUM(saldo_actual)
    //
    //    cantidad_creditos_asociado =
    //        COUNT(*)
    //
    // 3. Se obtiene el saldo de aportes sociales del cierre
    //    mensual de depósitos para la misma fecha de corte.
    //
    //    tipo_captacion_forma = '1'
    //
    // 4. El porcentaje se guarda como porcentaje real:
    //
    //    60 %  -> 60.000000
    //    40 %  -> 40.000000
    //
    // 5. El valor de aportes asignado al crédito:
    //
    //    saldo_aportes_fecha_corte
    //        *
    //    (saldo_credito / saldo_total_creditos_asociado)
    //
    // =========================================================

    public int calcularProrrateoAportes(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
        WITH creditos_cierre AS
        (
            SELECT
                f.id_cierre_cartera_credito,
                f.id_datos_personal,
                f.saldo_actual

            FROM cartera.cierres_cartera_creditos f

            WHERE f.id_cierre_cartera =
                  :idCierreCartera

              AND f.saldo_actual > 0
        ),

        saldos_creditos_asociado AS
        (
            SELECT
                c.id_datos_personal,

                SUM(
                    c.saldo_actual
                ) AS saldo_total_creditos_asociado,

                COUNT(*) AS cantidad_creditos_asociado

            FROM creditos_cierre c

            GROUP BY
                c.id_datos_personal
        ),

        fecha_cierre AS
        (
            SELECT
                c.fecha_corte

            FROM cartera.cierres_cartera c

            WHERE c.id_cierre_cartera =
                  :idCierreCartera
        ),

        aportes_asociado AS
        (
            SELECT
                d.id_datos_personal,

                SUM(
                    d.saldo_cierre
                ) AS saldo_aportes_fecha_corte

            FROM depositos.cierres_mensuales_detalle d

            INNER JOIN depositos.cierres_mensuales cm
                ON cm.id_cierre_mensual =
                   d.id_cierre_mensual

            INNER JOIN depositos.formas_ahorro fa
                ON fa.id_forma_ahorro =
                   d.id_forma_ahorro

            CROSS JOIN fecha_cierre fc

            WHERE cm.fecha_cierre =
                  fc.fecha_corte

              AND fa.tipo_captacion_forma = '1'

            GROUP BY
                d.id_datos_personal
        ),

        prorrateo_base AS
        (
            SELECT
                c.id_cierre_cartera_credito,
                c.id_datos_personal,
                c.saldo_actual
                    AS saldo_actual,

                s.cantidad_creditos_asociado,
                s.saldo_total_creditos_asociado,

                COALESCE(
                    a.saldo_aportes_fecha_corte,
                    0
                ) AS saldo_aportes_fecha_corte,

                CASE
                    WHEN s.saldo_total_creditos_asociado > 0
                    THEN ROUND(
                        (
                            c.saldo_actual
                            /
                            s.saldo_total_creditos_asociado
                        ) * 100,
                        6
                    )

                    ELSE 0
                END AS porcentaje_aportes_credito,

                CASE
                    WHEN s.saldo_total_creditos_asociado > 0
                    THEN ROUND(
                        COALESCE(
                            a.saldo_aportes_fecha_corte,
                            0
                        )
                        *
                        (
                            c.saldo_actual
                            /
                            s.saldo_total_creditos_asociado
                        ),
                        0
                    )

                    ELSE 0
                END AS valor_aportes_credito_base

            FROM creditos_cierre c

            INNER JOIN saldos_creditos_asociado s
                ON s.id_datos_personal =
                   c.id_datos_personal

            LEFT JOIN aportes_asociado a
                ON a.id_datos_personal =
                   c.id_datos_personal
        ),

        prorrateo_control AS
        (
            SELECT
                p.*,

                SUM(
                    p.valor_aportes_credito_base
                ) OVER
                (
                    PARTITION BY
                        p.id_datos_personal
                ) AS suma_aportes_redondeados,

                ROW_NUMBER() OVER
                (
                    PARTITION BY
                        p.id_datos_personal

                    ORDER BY
                        p.saldo_actual DESC,
                        p.id_cierre_cartera_credito
                ) AS fila_ajuste

            FROM prorrateo_base p
        ),

        prorrateo AS
        (
            SELECT
                p.id_cierre_cartera_credito,

                p.saldo_actual,

                p.cantidad_creditos_asociado,

                p.saldo_total_creditos_asociado,

                p.saldo_aportes_fecha_corte,

                p.porcentaje_aportes_credito,

                CASE
                    WHEN p.fila_ajuste = 1
                    THEN
                        p.valor_aportes_credito_base
                        +
                        (
                            p.saldo_aportes_fecha_corte
                            -
                            p.suma_aportes_redondeados
                        )

                    ELSE
                        p.valor_aportes_credito_base
                END AS valor_aportes_credito

            FROM prorrateo_control p
        )

        UPDATE cartera.cierres_cartera_resultados r

           SET saldo_actual =
                   p.saldo_actual,

               cantidad_creditos_asociado =
                   p.cantidad_creditos_asociado,

               saldo_total_creditos_asociado =
                   p.saldo_total_creditos_asociado,

               saldo_aportes_fecha_corte =
                   p.saldo_aportes_fecha_corte,

               porcentaje_aportes_credito =
                   p.porcentaje_aportes_credito,

               valor_aportes_credito =
                   p.valor_aportes_credito,

               fk_seguridad_edicion =
                   :idUsuario,

               fecha_edicion =
                   CURRENT_TIMESTAMP

          FROM prorrateo p

         WHERE r.id_cierre_cartera_credito =
               p.id_cierre_cartera_credito

           AND r.id_cierre_cartera =
               :idCierreCartera
        """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // LIMPIAR DETALLE DE GARANTÍAS DEL CIERRE
    // =========================================================

    public int limpiarProrrateoGarantias(
            Integer idCierreCartera
    ) {

        String sql = """
        DELETE FROM cartera.cierres_cartera_resultados_garantias
        WHERE id_cierre_cartera = :idCierreCartera
        """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // =========================================================
    // CALCULAR PRORRATEO DE GARANTÍAS
    //
    // REGLA:
    //
    // 1. Se trabaja exclusivamente sobre la fotografía
    //    del crédito del cierre.
    //
    // 2. Solo participan créditos cuya garantía fotografiada
    //    corresponde a garantía real:
    //        codigo_garantia_credito = '2'
    //
    // 3. La obligación jurídica fotografiada se identifica con:
    //
    //    id_agencia_juridica
    //    id_linea_credito_juridica
    //    pagare_juridico
    //
    // 4. Desde la obligación se obtienen:
    //
    //    obligaciones_fiadores
    //    obligaciones_fiadores_bienes
    //
    // 5. El valor del bien NO se toma del bien actual.
    //
    //    Se toma de:
    //        hoja_vida.cierres_hoja_vida_bienes
    //
    //    para la misma fecha de corte del cierre de cartera.
    //
    // 6. Por cada bien:
    //
    //    saldo_total_creditos_bien =
    //        SUM(saldo_actual de la fotografía)
    //
    //    porcentaje_credito_bien =
    //        saldo_credito
    //        / saldo_total_creditos_bien
    //        * 100
    //
    //    valor_garantia_credito_bien =
    //        valor_bien_fecha_corte
    //        * saldo_credito
    //        / saldo_total_creditos_bien
    //
    // =========================================================

    public int calcularProrrateoGarantias(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
        WITH creditos_bienes AS
        (
            SELECT
                f.id_cierre_cartera_credito,
                f.id_cierre_cartera,
                f.id_cartera_credito,
                f.saldo_actual,

                ofb.id_bien,

                MIN(
                    ofi.id_obligacion_fiador
                ) AS id_obligacion_fiador,

                MIN(
                    ofb.id_obligacion_fiador_bien
                ) AS id_obligacion_fiador_bien

            FROM cartera.cierres_cartera_creditos f

            INNER JOIN cartera.obligaciones_juridicas oj
                ON oj.id_agencia =
                   f.id_agencia_juridica

               AND oj.id_linea_credito =
                   f.id_linea_credito_juridica

               AND oj.numero_pagare =
                   f.pagare_juridico

            INNER JOIN cartera.obligaciones_fiadores ofi
                ON ofi.id_obligacion_juridica =
                   oj.id_obligacion_juridica

            INNER JOIN cartera.obligaciones_fiadores_bienes ofb
                ON ofb.id_obligacion_fiador =
                   ofi.id_obligacion_fiador

            WHERE f.id_cierre_cartera =
                  :idCierreCartera

              AND f.saldo_actual > 0

              AND f.codigo_garantia_credito = '2'

            GROUP BY
                f.id_cierre_cartera_credito,
                f.id_cierre_cartera,
                f.id_cartera_credito,
                f.saldo_actual,
                ofb.id_bien
        ),

        totales_bien AS
        (
            SELECT
                cb.id_bien,

                COUNT(
                    DISTINCT cb.id_cierre_cartera_credito
                ) AS cantidad_creditos_bien,

                SUM(
                    cb.saldo_actual
                ) AS saldo_total_creditos_bien

            FROM creditos_bienes cb

            GROUP BY
                cb.id_bien
        ),

        foto_bienes AS
        (
            SELECT
                chvb.id_bien,

                chvb.codigo_tipo_bien,
                chvb.nombre_tipo_bien,
                chvb.descripcion_general,

                COALESCE(
                    NULLIF(
                        TRIM(
                            chvb.numero_matricula_inmobiliaria
                        ),
                        ''
                    ),
                    NULLIF(
                        TRIM(
                            chvb.placa
                        ),
                        ''
                    ),
                    NULLIF(
                        TRIM(
                            chvb.serial_maquinaria
                        ),
                        ''
                    ),
                    NULLIF(
                        TRIM(
                            chvb.numero_titulo
                        ),
                        ''
                    ),
                    chvb.id_bien::varchar
                ) AS identificacion_bien,

                chvb.valor_comercial
                    AS valor_bien_fecha_corte

            FROM hoja_vida.cierres_hoja_vida_bienes chvb

            INNER JOIN hoja_vida.cierres_hoja_vida chv
                ON chv.id_cierre_hoja_vida =
                   chvb.id_cierre_hoja_vida

            INNER JOIN cartera.cierres_cartera cc
                ON cc.fecha_corte =
                   chv.fecha_corte

            WHERE cc.id_cierre_cartera =
                  :idCierreCartera
        ),

        distribucion_base AS
        (
            SELECT
                cb.id_cierre_cartera,
                cb.id_cierre_cartera_credito,

                cb.id_obligacion_fiador,
                cb.id_obligacion_fiador_bien,

                cb.id_bien,

                fb.codigo_tipo_bien
                    AS tipo_bien,

                fb.descripcion_general
                    AS descripcion_bien,

                fb.identificacion_bien,

                fb.valor_bien_fecha_corte,

                tb.cantidad_creditos_bien,

                tb.saldo_total_creditos_bien,

                cb.saldo_actual
                    AS saldo_credito,

                ROUND(
                    (
                        cb.saldo_actual * 100.0
                    )
                    /
                    NULLIF(
                        tb.saldo_total_creditos_bien,
                        0
                    ),
                    6
                ) AS porcentaje_credito_bien,

                ROUND(
                    fb.valor_bien_fecha_corte
                    *
                    (
                        cb.saldo_actual
                        /
                        NULLIF(
                            tb.saldo_total_creditos_bien,
                            0
                        )
                    ),
                    0
                ) AS valor_garantia_credito_bien_base

            FROM creditos_bienes cb

            INNER JOIN totales_bien tb
                ON tb.id_bien =
                   cb.id_bien

            INNER JOIN foto_bienes fb
                ON fb.id_bien =
                   cb.id_bien
        ),

        distribucion_control AS
        (
            SELECT
                d.*,

                SUM(
                    d.valor_garantia_credito_bien_base
                ) OVER
                (
                    PARTITION BY
                        d.id_bien
                ) AS suma_valor_distribuido,

                ROW_NUMBER() OVER
                (
                    PARTITION BY
                        d.id_bien

                    ORDER BY
                        d.saldo_credito DESC,
                        d.id_cierre_cartera_credito
                ) AS fila_ajuste

            FROM distribucion_base d
        ),

        distribucion AS
        (
            SELECT
                d.id_cierre_cartera,
                d.id_cierre_cartera_credito,

                d.id_obligacion_fiador,
                d.id_obligacion_fiador_bien,

                d.id_bien,

                d.tipo_bien,
                d.descripcion_bien,
                d.identificacion_bien,

                d.valor_bien_fecha_corte,

                d.cantidad_creditos_bien,
                d.saldo_total_creditos_bien,
                d.saldo_credito,

                d.porcentaje_credito_bien,

                CASE
                    WHEN d.fila_ajuste = 1
                    THEN
                        d.valor_garantia_credito_bien_base
                        +
                        (
                            d.valor_bien_fecha_corte
                            -
                            d.suma_valor_distribuido
                        )

                    ELSE
                        d.valor_garantia_credito_bien_base
                END AS valor_garantia_credito_bien

            FROM distribucion_control d
        )

        INSERT INTO cartera.cierres_cartera_resultados_garantias
        (
            id_cierre_cartera,
            id_cierre_cartera_resultado,
            id_cierre_cartera_credito,

            id_bien,

            id_obligacion_fiador,
            id_obligacion_fiador_bien,

            tipo_bien,
            descripcion_bien,
            identificacion_bien,

            valor_bien_fecha_corte,

            cantidad_creditos_bien,
            saldo_total_creditos_bien,
            saldo_credito,

            porcentaje_credito_bien,
            valor_garantia_credito_bien,

            fk_seguridad_creacion,
            fk_seguridad_edicion
        )

        SELECT
            d.id_cierre_cartera,
            r.id_cierre_cartera_resultado,
            d.id_cierre_cartera_credito,

            d.id_bien,

            d.id_obligacion_fiador,
            d.id_obligacion_fiador_bien,

            d.tipo_bien,
            d.descripcion_bien,
            d.identificacion_bien,

            d.valor_bien_fecha_corte,

            d.cantidad_creditos_bien,
            d.saldo_total_creditos_bien,
            d.saldo_credito,

            d.porcentaje_credito_bien,
            d.valor_garantia_credito_bien,

            :idUsuario,
            :idUsuario

        FROM distribucion d

        INNER JOIN cartera.cierres_cartera_resultados r
            ON r.id_cierre_cartera_credito =
               d.id_cierre_cartera_credito

           AND r.id_cierre_cartera =
               d.id_cierre_cartera
        """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // CONSOLIDAR GARANTÍAS POR CRÉDITO
    // =========================================================

    public int consolidarProrrateoGarantias(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
        WITH reinicio AS
        (
            UPDATE cartera.cierres_cartera_resultados

               SET cantidad_bienes_garantia = 0,
                   valor_garantias_total = 0,
                   porcentaje_garantias_credito = 0,
                   valor_garantias_credito = 0,

                   fk_seguridad_edicion =
                       :idUsuario,

                   fecha_edicion =
                       CURRENT_TIMESTAMP

             WHERE id_cierre_cartera =
                   :idCierreCartera

            RETURNING id_cierre_cartera_resultado
        ),

        consolidado AS
        (
            SELECT
                g.id_cierre_cartera_resultado,

                COUNT(
                    DISTINCT g.id_bien
                ) AS cantidad_bienes_garantia,

                SUM(
                    g.valor_bien_fecha_corte
                ) AS valor_garantias_total,

                SUM(
                    g.porcentaje_credito_bien
                ) AS porcentaje_garantias_credito,

                SUM(
                    g.valor_garantia_credito_bien
                ) AS valor_garantias_credito

            FROM cartera.cierres_cartera_resultados_garantias g

            WHERE g.id_cierre_cartera =
                  :idCierreCartera

            GROUP BY
                g.id_cierre_cartera_resultado
        )

        UPDATE cartera.cierres_cartera_resultados r

           SET cantidad_bienes_garantia =
                   c.cantidad_bienes_garantia,

               valor_garantias_total =
                   c.valor_garantias_total,

               porcentaje_garantias_credito =
                   c.porcentaje_garantias_credito,

               valor_garantias_credito =
                   c.valor_garantias_credito,

               fk_seguridad_edicion =
                   :idUsuario,

               fecha_edicion =
                   CURRENT_TIMESTAMP

          FROM consolidado c

         WHERE r.id_cierre_cartera_resultado =
               c.id_cierre_cartera_resultado

           AND r.id_cierre_cartera =
               :idCierreCartera
        """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

}