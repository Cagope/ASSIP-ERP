package co.assip.erp.cartera.calculosprevios;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import co.assip.erp.cartera.calculosprevios.dto.ResumenEdadMoraDTO;
import co.assip.erp.cartera.calculosprevios.dto.ResumenAportesGarantiasDTO;
import co.assip.erp.cartera.calculosprevios.dto.DetalleCalculosCierreDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
// VALIDAR CIERRE DE DEPÓSITOS EN FIRME
//
// Los cálculos de cartera dependen del cierre mensual
// de depósitos de la misma fecha de corte.
//
// Solamente se considera disponible cuando:
// - existe el cierre mensual de depósitos
// - corresponde exactamente a la fecha del cierre de cartera
// - estado_cierre = 'C'
// =========================================================

    public boolean existeCierreDepositosEnFirme(
            Integer idCierreCartera
    ) {

        String sql = """
        SELECT EXISTS
        (
            SELECT 1

            FROM cartera.cierres_cartera cc

            INNER JOIN depositos.cierres_mensuales cm
                ON cm.fecha_cierre =
                   cc.fecha_corte

            WHERE cc.id_cierre_cartera =
                  :idCierreCartera

              AND UPPER(
                    TRIM(cm.estado_cierre)
                  ) = 'C'
        )
        """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        Boolean existe =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Boolean.class
                );

        return Boolean.TRUE.equals(existe);
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

    // =========================================================
    // CONSOLIDAR COSTAS JUDICIALES
    //
    // REGLAS:
    //
    // 1. Las costas judiciales NO se causan mensualmente.
    //
    // 2. Se toma el saldo de los movimientos existentes
    //    hasta la fecha de corte.
    //
    // 3. Solamente participan movimientos activos:
    //
    //        estado = 'A'
    //
    // 4. Saldo:
    //
    //        SUM(valor_debito - valor_credito)
    //
    // 5. No se depende del estado actual de la cabecera
    //    de costas judiciales, porque una cabecera puede
    //    desactivarse posteriormente y no debe alterar
    //    un cierre histórico.
    //
    // 6. Todos los resultados del cierre se reinician
    //    primero a cero para permitir reejecución segura.
    //
    // =========================================================

    public int consolidarCostasJudiciales(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
            WITH fecha_cierre AS
            (
                SELECT
                    c.fecha_corte

                FROM cartera.cierres_cartera c

                WHERE c.id_cierre_cartera =
                      :idCierreCartera
            ),

            movimientos AS
            (
                SELECT
                    cj.id_cartera_credito,

                    SUM(
                        d.valor_debito
                        -
                        d.valor_credito
                    ) AS saldo_costas_judiciales

                FROM cartera.creditos_costas_judiciales cj

                INNER JOIN cartera.creditos_costas_judiciales_detalle d
                    ON d.id_credito_costa_judicial =
                       cj.id_credito_costa_judicial

                CROSS JOIN fecha_cierre fc

                WHERE d.estado = 'A'

                  AND d.fecha_movimiento <=
                      fc.fecha_corte

                GROUP BY
                    cj.id_cartera_credito
            )

            UPDATE cartera.cierres_cartera_resultados r

               SET valor_costas_judiciales =
                       COALESCE(
                           m.saldo_costas_judiciales,
                           0
                       ),

                   fk_seguridad_edicion =
                       :idUsuario,

                   fecha_edicion =
                       CURRENT_TIMESTAMP

              FROM cartera.cierres_cartera_creditos f

              LEFT JOIN movimientos m
                  ON m.id_cartera_credito =
                     f.id_cartera_credito

             WHERE f.id_cierre_cartera_credito =
                   r.id_cierre_cartera_credito

               AND f.id_cierre_cartera =
                   :idCierreCartera

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
    // RESUMEN POR CLASIFICACIÓN Y EDAD DE MORA
    //
    // REGLAS:
    //
    // 1. La información se consulta exclusivamente sobre los
    //    resultados persistidos del cierre.
    //
    // 2. Se agrupa por:
    //
    //    - clasificación del crédito
    //    - edad de mora calculada
    //
    // 3. Para cada clasificación siempre se presentan las
    //    edades A, B, C, D y E, aunque alguna no tenga créditos.
    //
    // 4. Los porcentajes se calculan dentro de cada
    //    clasificación.
    //
    // 5. Este método NO recalcula ni modifica información.
    // =========================================================

    public List<ResumenEdadMoraDTO> obtenerResumenEdadMora(
            Integer idCierreCartera
    ) {

        String sql = """
            WITH edades AS
            (
                SELECT 'A'::varchar AS edad_mora, 1 AS orden
                UNION ALL
                SELECT 'B'::varchar, 2
                UNION ALL
                SELECT 'C'::varchar, 3
                UNION ALL
                SELECT 'D'::varchar, 4
                UNION ALL
                SELECT 'E'::varchar, 5
            ),

            base AS
            (
                SELECT
                    f.codigo_clasificacion_credito,

                    r.edad_de_mora,

                    COALESCE(
                        r.saldo_actual,
                        f.saldo_actual,
                        0
                    ) AS saldo_actual

                FROM cartera.cierres_cartera_resultados r

                INNER JOIN cartera.cierres_cartera_creditos f
                    ON f.id_cierre_cartera_credito =
                       r.id_cierre_cartera_credito

                WHERE r.id_cierre_cartera =
                      :idCierreCartera

                  AND f.id_cierre_cartera =
                      :idCierreCartera

                  AND COALESCE(
                          f.saldo_actual,
                          0
                      ) > 0
            ),

            clasificaciones_cierre AS
            (
                SELECT DISTINCT
                    b.codigo_clasificacion_credito,

                    c.descripcion_clasificacion_credito

                FROM base b

                INNER JOIN cartera.clasificaciones_creditos c
                    ON c.codigo_clasificacion_credito =
                       b.codigo_clasificacion_credito
            ),

            combinaciones AS
            (
                SELECT
                    c.codigo_clasificacion_credito,
                    c.descripcion_clasificacion_credito,
                    e.edad_mora,
                    e.orden

                FROM clasificaciones_cierre c

                CROSS JOIN edades e
            ),

            agrupado AS
            (
                SELECT
                    b.codigo_clasificacion_credito,
                    UPPER(
                        TRIM(
                            b.edad_de_mora
                        )
                    ) AS edad_mora,

                    COUNT(*) AS cantidad_creditos,

                    SUM(
                        b.saldo_actual
                    ) AS saldo_capital

                FROM base b

                WHERE UPPER(
                          TRIM(
                              COALESCE(
                                  b.edad_de_mora,
                                  ''
                              )
                          )
                      ) IN ('A', 'B', 'C', 'D', 'E')

                GROUP BY
                    b.codigo_clasificacion_credito,

                    UPPER(
                        TRIM(
                            b.edad_de_mora
                        )
                    )
            ),

            totales AS
            (
                SELECT
                    b.codigo_clasificacion_credito,

                    COUNT(*) AS cantidad_total,

                    SUM(
                        b.saldo_actual
                    ) AS saldo_total

                FROM base b

                GROUP BY
                    b.codigo_clasificacion_credito
            )

            SELECT
                c.codigo_clasificacion_credito,

                c.descripcion_clasificacion_credito,

                c.edad_mora,

                COALESCE(
                    a.cantidad_creditos,
                    0
                )::integer AS cantidad_creditos,

                COALESCE(
                    a.saldo_capital,
                    0
                ) AS saldo_capital,

                CASE
                    WHEN COALESCE(
                             t.cantidad_total,
                             0
                         ) > 0
                    THEN ROUND(
                        (
                            COALESCE(
                                a.cantidad_creditos,
                                0
                            )::numeric
                            /
                            t.cantidad_total::numeric
                        ) * 100,
                        2
                    )

                    ELSE 0
                END AS porcentaje_cantidad,

                CASE
                    WHEN COALESCE(
                             t.saldo_total,
                             0
                         ) > 0
                    THEN ROUND(
                        (
                            COALESCE(
                                a.saldo_capital,
                                0
                            )
                            /
                            t.saldo_total
                        ) * 100,
                        2
                    )

                    ELSE 0
                END AS porcentaje_saldo

            FROM combinaciones c

            INNER JOIN totales t
                ON t.codigo_clasificacion_credito =
                   c.codigo_clasificacion_credito

            LEFT JOIN agrupado a
                ON a.codigo_clasificacion_credito =
                   c.codigo_clasificacion_credito

               AND a.edad_mora =
                   c.edad_mora

            ORDER BY
                c.codigo_clasificacion_credito,
                c.orden
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        return jdbc.query(
                sql,
                parametros,
                (rs, rowNum) ->
                        new ResumenEdadMoraDTO(
                                rs.getString(
                                        "codigo_clasificacion_credito"
                                ),
                                rs.getString(
                                        "descripcion_clasificacion_credito"
                                ),
                                rs.getString(
                                        "edad_mora"
                                ),
                                rs.getInt(
                                        "cantidad_creditos"
                                ),
                                rs.getBigDecimal(
                                        "saldo_capital"
                                ),
                                rs.getBigDecimal(
                                        "porcentaje_cantidad"
                                ),
                                rs.getBigDecimal(
                                        "porcentaje_saldo"
                                )
                        )
        );
    }

    // =========================================================
    // RESUMEN DE APORTES Y GARANTÍAS
    //
    // REGLAS:
    //
    // APORTES
    //
    // 1. Créditos con aportes:
    //    valor_aportes_credito > 0.
    //
    // 2. El saldo disponible de aportes se suma una sola vez
    //    por asociado, porque un asociado puede tener varios
    //    créditos dentro del cierre.
    //
    // 3. El valor prorrateado corresponde a la suma de
    //    valor_aportes_credito de todos los créditos.
    //
    // GARANTÍAS
    //
    // 4. Un crédito tiene garantía cuando posee al menos un
    //    bien consolidado.
    //
    // 5. La cantidad de bienes corresponde a id_bien distintos.
    //
    // 6. El valor de los bienes se suma una sola vez por bien,
    //    evitando duplicarlo cuando respalda varios créditos.
    //
    // 7. El valor asignado corresponde al valor de garantía
    //    efectivamente prorrateado a los créditos.
    //
    // Este método solamente consulta información persistida.
    // NO recalcula ni modifica el cierre.
    // =========================================================

    public ResumenAportesGarantiasDTO obtenerResumenAportesGarantias(
            Integer idCierreCartera
    ) {

        String sql = """
            WITH resultados AS
            (
                SELECT
                    r.id_cierre_cartera_resultado,
                    r.id_cierre_cartera_credito,

                    f.id_datos_personal,

                    COALESCE(
                        r.saldo_aportes_fecha_corte,
                        0
                    ) AS saldo_aportes_fecha_corte,

                    COALESCE(
                        r.valor_aportes_credito,
                        0
                    ) AS valor_aportes_credito,

                    COALESCE(
                        r.cantidad_bienes_garantia,
                        0
                    ) AS cantidad_bienes_garantia,

                    COALESCE(
                        r.valor_garantias_credito,
                        0
                    ) AS valor_garantias_credito

                FROM cartera.cierres_cartera_resultados r

                INNER JOIN cartera.cierres_cartera_creditos f
                    ON f.id_cierre_cartera_credito =
                       r.id_cierre_cartera_credito

                   AND f.id_cierre_cartera =
                       r.id_cierre_cartera

                WHERE r.id_cierre_cartera =
                      :idCierreCartera
            ),

            aportes_creditos AS
            (
                SELECT
                    COUNT(*) FILTER
                    (
                        WHERE valor_aportes_credito > 0
                    ) AS creditos_con_aportes,

                    COUNT(*) FILTER
                    (
                        WHERE valor_aportes_credito <= 0
                    ) AS creditos_sin_aportes,

                    COALESCE(
                        SUM(
                            valor_aportes_credito
                        ),
                        0
                    ) AS valor_aportes_prorrateado

                FROM resultados
            ),

            aportes_persona AS
            (
                SELECT
                    id_datos_personal,

                    MAX(
                        saldo_aportes_fecha_corte
                    ) AS saldo_aportes_fecha_corte

                FROM resultados

                GROUP BY
                    id_datos_personal
            ),

            aportes_totales AS
            (
                SELECT
                    COALESCE(
                        SUM(
                            saldo_aportes_fecha_corte
                        ),
                        0
                    ) AS saldo_aportes_disponible

                FROM aportes_persona
            ),

            garantias_creditos AS
            (
                SELECT
                    COUNT(*) FILTER
                    (
                        WHERE cantidad_bienes_garantia > 0
                    ) AS creditos_con_garantia,

                    COUNT(*) FILTER
                    (
                        WHERE cantidad_bienes_garantia <= 0
                    ) AS creditos_sin_garantia,

                    COALESCE(
                        SUM(
                            valor_garantias_credito
                        ),
                        0
                    ) AS valor_garantias_asignado

                FROM resultados
            ),

            bienes_unicos AS
            (
                SELECT
                    g.id_bien,

                    MAX(
                        COALESCE(
                            g.valor_bien_fecha_corte,
                            0
                        )
                    ) AS valor_bien_fecha_corte

                FROM cartera.cierres_cartera_resultados_garantias g

                WHERE g.id_cierre_cartera =
                      :idCierreCartera

                  AND g.id_bien IS NOT NULL

                GROUP BY
                    g.id_bien
            ),

            garantias_bienes AS
            (
                SELECT
                    COUNT(*)::integer
                        AS cantidad_bienes_garantia,

                    COALESCE(
                        SUM(
                            valor_bien_fecha_corte
                        ),
                        0
                    ) AS valor_bienes_garantia

                FROM bienes_unicos
            )

            SELECT
                COALESCE(
                    ac.creditos_con_aportes,
                    0
                )::integer
                    AS creditos_con_aportes,

                COALESCE(
                    ac.creditos_sin_aportes,
                    0
                )::integer
                    AS creditos_sin_aportes,

                COALESCE(
                    at.saldo_aportes_disponible,
                    0
                )
                    AS saldo_aportes_disponible,

                COALESCE(
                    ac.valor_aportes_prorrateado,
                    0
                )
                    AS valor_aportes_prorrateado,

                COALESCE(
                    gc.creditos_con_garantia,
                    0
                )::integer
                    AS creditos_con_garantia,

                COALESCE(
                    gc.creditos_sin_garantia,
                    0
                )::integer
                    AS creditos_sin_garantia,

                COALESCE(
                    gb.cantidad_bienes_garantia,
                    0
                )::integer
                    AS cantidad_bienes_garantia,

                COALESCE(
                    gb.valor_bienes_garantia,
                    0
                )
                    AS valor_bienes_garantia,

                COALESCE(
                    gc.valor_garantias_asignado,
                    0
                )
                    AS valor_garantias_asignado

            FROM aportes_creditos ac

            CROSS JOIN aportes_totales at

            CROSS JOIN garantias_creditos gc

            CROSS JOIN garantias_bienes gb
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        return jdbc.queryForObject(
                sql,
                parametros,
                (rs, rowNum) ->
                        new ResumenAportesGarantiasDTO(

                                rs.getInt(
                                        "creditos_con_aportes"
                                ),

                                rs.getInt(
                                        "creditos_sin_aportes"
                                ),

                                rs.getBigDecimal(
                                        "saldo_aportes_disponible"
                                ),

                                rs.getBigDecimal(
                                        "valor_aportes_prorrateado"
                                ),

                                rs.getInt(
                                        "creditos_con_garantia"
                                ),

                                rs.getInt(
                                        "creditos_sin_garantia"
                                ),

                                rs.getInt(
                                        "cantidad_bienes_garantia"
                                ),

                                rs.getBigDecimal(
                                        "valor_bienes_garantia"
                                ),

                                rs.getBigDecimal(
                                        "valor_garantias_asignado"
                                )
                        )
        );
    }

    // =========================================================
    // DETALLE COMPLETO DE CÁLCULOS DEL CIERRE
    //
    // Fuente principal para:
    //
    // - Excel de revisión.
    // - Auditoría del cierre.
    // - Comparación fotografía vs cálculos.
    //
    // IMPORTANTE:
    //
    // - Solamente consulta información persistida.
    // - NO recalcula.
    // - NO modifica el cierre.
    // - Una fila por crédito fotografiado.
    // =========================================================

    public List<DetalleCalculosCierreDTO> obtenerDetalleCalculosCierre(
            Integer idCierreCartera
    ) {

        String sql = """
            SELECT

                -- =============================================
                -- CIERRE
                -- =============================================

                c.id_cierre_cartera,
                c.fecha_corte,

                -- =============================================
                -- IDENTIFICACIÓN
                -- =============================================

                f.id_cartera_credito,
                f.id_cierre_cartera_credito,
                r.id_cierre_cartera_resultado,
                f.id_agencia,

                f.pagare_cartera,

                f.id_datos_personal,
                f.tipo_documento,
                f.documento,
                f.nombres,
                f.primer_apellido,
                f.segundo_apellido,

                TRIM(
                    CONCAT_WS(
                        ' ',
                        NULLIF(TRIM(f.nombres), ''),
                        NULLIF(TRIM(f.primer_apellido), ''),
                        NULLIF(TRIM(f.segundo_apellido), '')
                    )
                ) AS nombre_completo,

                -- =============================================
                -- LÍNEA Y CLASIFICACIÓN
                -- =============================================

                f.id_linea_credito,
                f.codigo_linea_credito,
                f.nombre_linea_credito,

                f.codigo_clasificacion_credito,
                f.descripcion_clasificacion_credito,

                f.codigo_garantia_credito,
                f.descripcion_garantia_credito,
                f.tipo_garantia,

                f.codigo_subgarantia,
                f.descripcion_subgarantia,

                f.codigo_destino_economico,
                f.descripcion_destino_economico,

                -- =============================================
                -- CONDICIONES FINANCIERAS
                -- =============================================

                f.tipo_modalidad_interes,
                f.descripcion_modalidad_interes,
                f.periodo_codigo_interes,
                f.periodo_meses_interes,

                f.amortizacion_capital,

                f.codigo_tipo_cuota,
                f.descripcion_tipo_cuota,

                f.plazo,
                f.meses_gracia_capital,
                f.meses_gracia_interes,

                f.codigo_forma_pago,
                f.descripcion_forma_pago,

                f.tasa_nominal_anual,
                f.tasa_efectiva_anual,

                -- =============================================
                -- ORIGEN / APROBACIÓN
                -- =============================================

                f.id_empresa_libranza,
                f.documento_empresa_libranza,

                f.id_ente_aprobacion,
                f.nombre_ente_aprobacion,

                f.tipo_comprobante,
                f.numero_comprobante,

                -- =============================================
                -- ESTADO
                -- =============================================

                f.codigo_estado_cartera,
                f.descripcion_estado_cartera,

                f.codigo_estado_juridico,
                f.descripcion_estado_juridico,
                f.fecha_estado_juridico,

                f.codigo_modificacion_credito,
                f.descripcion_modificacion_credito,
                f.numero_novaciones,

                -- =============================================
                -- VALORES DEL CRÉDITO
                -- =============================================

                f.valor_inicial_credito,
                f.valor_desembolsado,
                f.valor_base_calculo_cuota,
                f.valor_primera_cuota,
                f.valor_cuota,

                f.saldo_actual
                    AS saldo_fotografia,

                f.abonos_pendientes,

                f.altura_cuota,

                -- =============================================
                -- FECHAS
                -- =============================================

                f.fecha_inclusion_sistema,
                f.fecha_contable,
                f.fecha_desembolso,
                f.fecha_primera_cuota,
                f.fecha_primera_cuota_capital,
                f.fecha_primera_cuota_interes,
                f.fecha_final,

                f.ultima_fecha_capital,
                f.ultima_fecha_interes,
                f.ultima_fecha_mora,
                f.ultima_fecha_seguro,
                f.ultima_fecha_fondo,

                f.proxima_fecha_capital,
                f.proxima_fecha_interes,
                f.proxima_fecha_seguro,
                f.proxima_fecha_fondo,

                f.intereses_pagados_hasta,
                f.intereses_mora_hasta,

                -- =============================================
                -- EVALUACIÓN / RIESGO FOTOGRAFÍA
                -- =============================================

                f.credito_evaluado,
                f.fecha_evaluacion,

                f.edad_de_riesgo
                    AS edad_riesgo_fotografia,

                f.edad_riesgo_inicial
                    AS edad_riesgo_inicial_fotografia,

                f.edad_de_mora
                    AS edad_mora_fotografia,

                f.edad_de_pe
                    AS edad_pe_fotografia,

                f.edad_de_homologacion
                    AS edad_homologacion_fotografia,

                f.edad_contable
                    AS edad_contable_fotografia,

                -- =============================================
                -- REESTRUCTURACIÓN FOTOGRAFÍA
                -- =============================================

                f.credito_reestructurado
                    AS credito_reestructurado_fotografia,

                f.fecha_reestructuracion,

                f.edad_reestructuracion_inicial
                    AS edad_reestructuracion_inicial_fotografia,

                f.edad_reestructurado
                    AS edad_reestructurado_fotografia,

                -- =============================================
                -- CÁLCULOS DEL CIERRE
                -- =============================================

                r.es_una_sola_cuota,
                r.es_reestructurado,

                r.dias_mora,
                r.dias_diferencia,

                r.edad_riesgo_inicial
                    AS edad_riesgo_inicial_calculada,

                r.edad_de_mora
                    AS edad_mora_calculada,

                r.edad_de_riesgo
                    AS edad_riesgo_calculada,

                r.edad_reestructuracion_inicial
                    AS edad_reestructuracion_inicial_calculada,

                r.edad_reestructurado
                    AS edad_reestructurado_calculada,

                -- =============================================
                -- APORTES
                -- =============================================

                r.cantidad_creditos_asociado,
                r.saldo_total_creditos_asociado,
                r.saldo_aportes_fecha_corte,
                r.porcentaje_aportes_credito,
                r.valor_aportes_credito,

                -- =============================================
                -- GARANTÍAS
                -- =============================================

                r.cantidad_bienes_garantia,
                r.valor_garantias_total,
                r.porcentaje_garantias_credito,
                r.valor_garantias_credito,

                -- =============================================
                -- OTROS VALORES
                -- =============================================

                r.saldo_intereses_causados,
                r.valor_intereses_causados_mes,

                r.saldo_intereses_contingentes,
                r.valor_intereses_contingentes_mes,

                r.valor_costas_judiciales,

                r.saldo_seguros,
                r.valor_seguros_mes,

                r.saldo_alivios,
                r.valor_alivios_mes,

                r.valor_fondos_garantias,
                r.valor_otros_conceptos,

                -- =============================================
                -- RESULTADOS POSTERIORES
                -- =============================================

                r.edad_de_pe
                    AS edad_pe_resultado,

                r.edad_de_homologacion
                    AS edad_homologacion_resultado,

                r.edad_contable
                    AS edad_contable_resultado,

                r.vea,
                r.pi,
                r.pdi,
                r.perdida_esperada,

                r.deterioro_capital,
                r.deterioro_intereses,
                r.deterioro_otros,

                r.codigo_metodo_calculo,

                -- =============================================
                -- CONTROL
                -- =============================================

                r.fecha_calculo

            FROM cartera.cierres_cartera_creditos f

            INNER JOIN cartera.cierres_cartera c
                ON c.id_cierre_cartera =
                   f.id_cierre_cartera

            LEFT JOIN cartera.cierres_cartera_resultados r
                ON r.id_cierre_cartera =
                   f.id_cierre_cartera

                AND r.id_cierre_cartera_credito =
                    f.id_cierre_cartera_credito

            WHERE f.id_cierre_cartera = :idCierreCartera
                 AND COALESCE(f.saldo_actual, 0) > 0

            ORDER BY
                f.codigo_clasificacion_credito,
                f.documento,
                f.pagare_cartera,
                f.id_cartera_credito
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        );

        return jdbc.query(
                sql,
                parametros,
                (rs, rowNum) ->
                        new DetalleCalculosCierreDTO(

                                // =============================
                                // CIERRE
                                // =============================

                                rs.getObject(
                                    "id_cierre_cartera",
                                    Integer.class
                                ),

                                rs.getObject(
                                        "fecha_corte",
                                        LocalDate.class
                                ),

                                // =============================
                                // IDENTIFICACIÓN
                                // =============================

                                rs.getInt(
                                        "id_cartera_credito"
                                ),

                                rs.getInt(
                                        "id_cierre_cartera_credito"
                                ),

                                rs.getInt(
                                        "id_cierre_cartera_resultado"
                                ),

                                rs.getInt(
                                        "id_agencia"
                                ),

                                rs.getString(
                                        "pagare_cartera"
                                ),

                                rs.getObject(
                                        "id_datos_personal",
                                        Integer.class
                                ),

                                rs.getString(
                                        "tipo_documento"
                                ),

                                rs.getString(
                                        "documento"
                                ),

                                rs.getString(
                                        "nombres"
                                ),

                                rs.getString(
                                        "primer_apellido"
                                ),

                                rs.getString(
                                        "segundo_apellido"
                                ),

                                rs.getString(
                                        "nombre_completo"
                                ),

                                // =============================
                                // LÍNEA Y CLASIFICACIÓN
                                // =============================

                                rs.getObject(
                                        "id_linea_credito",
                                        Integer.class
                                ),

                                rs.getString(
                                        "codigo_linea_credito"
                                ),

                                rs.getString(
                                        "nombre_linea_credito"
                                ),

                                rs.getString(
                                        "codigo_clasificacion_credito"
                                ),

                                rs.getString(
                                        "descripcion_clasificacion_credito"
                                ),

                                rs.getString(
                                        "codigo_garantia_credito"
                                ),

                                rs.getString(
                                        "descripcion_garantia_credito"
                                ),

                                rs.getString(
                                        "tipo_garantia"
                                ),

                                rs.getString(
                                        "codigo_subgarantia"
                                ),

                                rs.getString(
                                        "descripcion_subgarantia"
                                ),

                                rs.getString(
                                        "codigo_destino_economico"
                                ),

                                rs.getString(
                                        "descripcion_destino_economico"
                                ),

                                // =============================
                                // CONDICIONES FINANCIERAS
                                // =============================

                                rs.getString(
                                        "tipo_modalidad_interes"
                                ),

                                rs.getString(
                                        "descripcion_modalidad_interes"
                                ),

                                rs.getString(
                                        "periodo_codigo_interes"
                                ),

                                rs.getObject(
                                        "periodo_meses_interes",
                                        Integer.class
                                ),

                                rs.getObject(
                                        "amortizacion_capital",
                                        Integer.class
                                ),

                                rs.getString(
                                        "codigo_tipo_cuota"
                                ),

                                rs.getString(
                                        "descripcion_tipo_cuota"
                                ),

                                rs.getObject(
                                        "plazo",
                                        Integer.class
                                ),

                                rs.getObject(
                                        "meses_gracia_capital",
                                        Integer.class
                                ),

                                rs.getObject(
                                        "meses_gracia_interes",
                                        Integer.class
                                ),

                                rs.getString(
                                        "codigo_forma_pago"
                                ),

                                rs.getString(
                                        "descripcion_forma_pago"
                                ),

                                rs.getBigDecimal(
                                        "tasa_nominal_anual"
                                ),

                                rs.getBigDecimal(
                                        "tasa_efectiva_anual"
                                ),

                                // =============================
                                // ORIGEN / APROBACIÓN
                                // =============================

                                rs.getObject(
                                        "id_empresa_libranza",
                                        Integer.class
                                ),

                                rs.getString(
                                        "documento_empresa_libranza"
                                ),

                                rs.getObject(
                                        "id_ente_aprobacion",
                                        Integer.class
                                ),

                                rs.getString(
                                        "nombre_ente_aprobacion"
                                ),

                                rs.getString(
                                        "tipo_comprobante"
                                ),

                                rs.getString(
                                        "numero_comprobante"
                                ),

                                // =============================
                                // ESTADO
                                // =============================

                                rs.getString(
                                        "codigo_estado_cartera"
                                ),

                                rs.getString(
                                        "descripcion_estado_cartera"
                                ),

                                rs.getString(
                                        "codigo_estado_juridico"
                                ),

                                rs.getString(
                                        "descripcion_estado_juridico"
                                ),

                                rs.getObject(
                                        "fecha_estado_juridico",
                                        LocalDate.class
                                ),

                                rs.getString(
                                        "codigo_modificacion_credito"
                                ),

                                rs.getString(
                                        "descripcion_modificacion_credito"
                                ),

                                rs.getObject(
                                        "numero_novaciones",
                                        Integer.class
                                ),

                                // =============================
                                // VALORES
                                // =============================

                                rs.getBigDecimal(
                                        "valor_inicial_credito"
                                ),

                                rs.getBigDecimal(
                                        "valor_desembolsado"
                                ),

                                rs.getBigDecimal(
                                        "valor_base_calculo_cuota"
                                ),

                                rs.getBigDecimal(
                                        "valor_primera_cuota"
                                ),

                                rs.getBigDecimal(
                                        "valor_cuota"
                                ),

                                rs.getBigDecimal(
                                        "saldo_fotografia"
                                ),

                                rs.getBigDecimal(
                                        "abonos_pendientes"
                                ),

                                rs.getObject(
                                        "altura_cuota",
                                        Integer.class
                                ),

                                // =============================
                                // FECHAS
                                // =============================

                                rs.getObject(
                                        "fecha_inclusion_sistema",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "fecha_contable",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "fecha_desembolso",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "fecha_primera_cuota",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "fecha_primera_cuota_capital",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "fecha_primera_cuota_interes",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "fecha_final",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "ultima_fecha_capital",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "ultima_fecha_interes",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "ultima_fecha_mora",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "ultima_fecha_seguro",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "ultima_fecha_fondo",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "proxima_fecha_capital",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "proxima_fecha_interes",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "proxima_fecha_seguro",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "proxima_fecha_fondo",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "intereses_pagados_hasta",
                                        LocalDate.class
                                ),

                                rs.getObject(
                                        "intereses_mora_hasta",
                                        LocalDate.class
                                ),

                                // =============================
                                // EVALUACIÓN / FOTO
                                // =============================

                                rs.getObject(
                                        "credito_evaluado",
                                        Boolean.class
                                ),

                                rs.getObject(
                                        "fecha_evaluacion",
                                        LocalDate.class
                                ),

                                rs.getString(
                                        "edad_riesgo_fotografia"
                                ),

                                rs.getString(
                                        "edad_riesgo_inicial_fotografia"
                                ),

                                rs.getString(
                                        "edad_mora_fotografia"
                                ),

                                rs.getString(
                                        "edad_pe_fotografia"
                                ),

                                rs.getString(
                                        "edad_homologacion_fotografia"
                                ),

                                rs.getString(
                                        "edad_contable_fotografia"
                                ),

                                // =============================
                                // REESTRUCTURACIÓN FOTO
                                // =============================

                                rs.getObject(
                                        "credito_reestructurado_fotografia",
                                        Boolean.class
                                ),

                                rs.getObject(
                                        "fecha_reestructuracion",
                                        LocalDate.class
                                ),

                                rs.getString(
                                        "edad_reestructuracion_inicial_fotografia"
                                ),

                                rs.getString(
                                        "edad_reestructurado_fotografia"
                                ),

                                // =============================
                                // CÁLCULOS
                                // =============================

                                rs.getObject(
                                        "es_una_sola_cuota",
                                        Boolean.class
                                ),

                                rs.getObject(
                                        "es_reestructurado",
                                        Boolean.class
                                ),

                                rs.getObject(
                                        "dias_mora",
                                        Integer.class
                                ),

                                rs.getObject(
                                        "dias_diferencia",
                                        Integer.class
                                ),

                                rs.getString(
                                        "edad_riesgo_inicial_calculada"
                                ),

                                rs.getString(
                                        "edad_mora_calculada"
                                ),

                                rs.getString(
                                        "edad_riesgo_calculada"
                                ),

                                rs.getString(
                                        "edad_reestructuracion_inicial_calculada"
                                ),

                                rs.getString(
                                        "edad_reestructurado_calculada"
                                ),

                                // =============================
                                // APORTES
                                // =============================

                                rs.getObject(
                                        "cantidad_creditos_asociado",
                                        Integer.class
                                ),

                                rs.getBigDecimal(
                                        "saldo_total_creditos_asociado"
                                ),

                                rs.getBigDecimal(
                                        "saldo_aportes_fecha_corte"
                                ),

                                rs.getBigDecimal(
                                        "porcentaje_aportes_credito"
                                ),

                                rs.getBigDecimal(
                                        "valor_aportes_credito"
                                ),

                                // =============================
                                // GARANTÍAS
                                // =============================

                                rs.getObject(
                                        "cantidad_bienes_garantia",
                                        Integer.class
                                ),

                                rs.getBigDecimal(
                                        "valor_garantias_total"
                                ),

                                rs.getBigDecimal(
                                        "porcentaje_garantias_credito"
                                ),

                                rs.getBigDecimal(
                                        "valor_garantias_credito"
                                ),

                                // =============================
                                // OTROS VALORES
                                // =============================

                                rs.getBigDecimal(
                                        "saldo_intereses_causados"
                                ),

                                rs.getBigDecimal(
                                        "valor_intereses_causados_mes"
                                ),

                                rs.getBigDecimal(
                                        "saldo_intereses_contingentes"
                                ),

                                rs.getBigDecimal(
                                        "valor_intereses_contingentes_mes"
                                ),

                                rs.getBigDecimal(
                                        "valor_costas_judiciales"
                                ),

                                rs.getBigDecimal(
                                        "saldo_seguros"
                                ),

                                rs.getBigDecimal(
                                        "valor_seguros_mes"
                                ),

                                rs.getBigDecimal(
                                        "saldo_alivios"
                                ),

                                rs.getBigDecimal(
                                        "valor_alivios_mes"
                                ),

                                rs.getBigDecimal(
                                        "valor_fondos_garantias"
                                ),

                                rs.getBigDecimal(
                                        "valor_otros_conceptos"
                                ),

                                // =============================
                                // RESULTADOS POSTERIORES
                                // =============================

                                rs.getString(
                                        "edad_pe_resultado"
                                ),

                                rs.getString(
                                        "edad_homologacion_resultado"
                                ),

                                rs.getString(
                                        "edad_contable_resultado"
                                ),

                                rs.getBigDecimal(
                                        "vea"
                                ),

                                rs.getBigDecimal(
                                        "pi"
                                ),

                                rs.getBigDecimal(
                                        "pdi"
                                ),

                                rs.getBigDecimal(
                                        "perdida_esperada"
                                ),

                                rs.getBigDecimal(
                                        "deterioro_capital"
                                ),

                                rs.getBigDecimal(
                                        "deterioro_intereses"
                                ),

                                rs.getBigDecimal(
                                        "deterioro_otros"
                                ),

                                rs.getString(
                                        "codigo_metodo_calculo"
                                ),

                                // =============================
                                // CONTROL
                                // =============================

                                rs.getObject(
                                        "fecha_calculo",
                                        LocalDateTime.class
                                )
                        )
        );
    }

}