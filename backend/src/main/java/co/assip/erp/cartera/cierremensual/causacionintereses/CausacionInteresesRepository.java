package co.assip.erp.cartera.cierremensual.causacionintereses;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CausacionInteresesRepository {

    private static final String OBS_CAUSADOS =
            "CAUSACION INTERESES CIERRE";

    private static final String OBS_CONTINGENTES =
            "CAUSACION INTERESES CONTINGENTES CIERRE";

    private final NamedParameterJdbcTemplate jdbc;

    public CausacionInteresesRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // VALIDAR MOVIMIENTOS YA CONTABILIZADOS
    //
    // Si una causación automática del mismo cierre ya tiene
    // comprobante, NO debe eliminarse para recalcular.
    // =========================================================

    public int contarMovimientosContabilizados(
            Integer idCierreCartera
    ) {

        String sql = """
            WITH fecha_cierre AS
            (
                SELECT
                    fecha_corte
                FROM cartera.cierres_cartera
                WHERE id_cierre_cartera =
                      :idCierreCartera
            ),

            creditos_cierre AS
            (
                SELECT DISTINCT
                    id_cartera_credito
                FROM cartera.cierres_cartera_creditos
                WHERE id_cierre_cartera =
                      :idCierreCartera
            ),

            contabilizados AS
            (
                SELECT
                    ic.id_interes_causado
                FROM cartera.creditos_intereses_causados ic

                INNER JOIN creditos_cierre c
                    ON c.id_cartera_credito =
                       ic.id_cartera_credito

                CROSS JOIN fecha_cierre fc

                WHERE ic.fecha_movimiento =
                      fc.fecha_corte

                  AND ic.observacion_interes =
                      :observacionCausados

                  AND (
                        NULLIF(
                            TRIM(ic.tipo_comprobante),
                            ''
                        ) IS NOT NULL

                        OR

                        NULLIF(
                            TRIM(ic.numero_comprobante),
                            ''
                        ) IS NOT NULL
                  )

                UNION ALL

                SELECT
                    ict.id_interes_contingente
                FROM cartera.creditos_intereses_contingentes ict

                INNER JOIN creditos_cierre c
                    ON c.id_cartera_credito =
                       ict.id_cartera_credito

                CROSS JOIN fecha_cierre fc

                WHERE ict.fecha_movimiento =
                      fc.fecha_corte

                  AND ict.observacion_interes =
                      :observacionContingentes

                  AND (
                        NULLIF(
                            TRIM(ict.tipo_comprobante),
                            ''
                        ) IS NOT NULL

                        OR

                        NULLIF(
                            TRIM(ict.numero_comprobante),
                            ''
                        ) IS NOT NULL
                  )
            )

            SELECT COUNT(*)
            FROM contabilizados
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "observacionCausados",
                                OBS_CAUSADOS
                        )
                        .addValue(
                                "observacionContingentes",
                                OBS_CONTINGENTES
                        );

        Integer cantidad =
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Integer.class
                );

        return cantidad == null
                ? 0
                : cantidad;
    }

    // =========================================================
    // LIMPIAR CAUSACIÓN AUTOMÁTICA DEL CIERRE
    //
    // IMPORTANTE:
    //
    // - NO elimina pagos.
    // - NO elimina ajustes manuales.
    // - NO elimina movimientos con comprobante.
    // - Solo elimina movimientos automáticos generados
    //   nuevamente para la misma fecha de corte.
    // =========================================================

    public int limpiarCausacionCierre(
            Integer idCierreCartera
    ) {

        String sql = """
            DELETE FROM cartera.creditos_intereses_causados ic

            USING
                cartera.cierres_cartera_creditos f,
                cartera.cierres_cartera cc

            WHERE f.id_cierre_cartera =
                  :idCierreCartera

              AND cc.id_cierre_cartera =
                  f.id_cierre_cartera

              AND ic.id_cartera_credito =
                  f.id_cartera_credito

              AND ic.fecha_movimiento =
                  cc.fecha_corte

              AND ic.observacion_interes =
                  :observacion

              AND NULLIF(
                    TRIM(ic.tipo_comprobante),
                    ''
                  ) IS NULL

              AND NULLIF(
                    TRIM(ic.numero_comprobante),
                    ''
                  ) IS NULL
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "observacion",
                                OBS_CAUSADOS
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // LIMPIAR CONTINGENTES AUTOMÁTICOS DEL CIERRE
    // =========================================================

    public int limpiarContingentesCierre(
            Integer idCierreCartera
    ) {

        String sql = """
            DELETE FROM cartera.creditos_intereses_contingentes ic

            USING
                cartera.cierres_cartera_creditos f,
                cartera.cierres_cartera cc

            WHERE f.id_cierre_cartera =
                  :idCierreCartera

              AND cc.id_cierre_cartera =
                  f.id_cierre_cartera

              AND ic.id_cartera_credito =
                  f.id_cartera_credito

              AND ic.fecha_movimiento =
                  cc.fecha_corte

              AND ic.observacion_interes =
                  :observacion

              AND NULLIF(
                    TRIM(ic.tipo_comprobante),
                    ''
                  ) IS NULL

              AND NULLIF(
                    TRIM(ic.numero_comprobante),
                    ''
                  ) IS NULL
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idCierreCartera",
                                idCierreCartera
                        )
                        .addValue(
                                "observacion",
                                OBS_CONTINGENTES
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // CAUSAR INTERESES
    //
    // Replica la lógica vigente del ERP anterior para intereses:
    //
    // VIGENTES (categoría A):
    // - fecha inicial: ultima_fecha_interes
    // - fecha final: fecha_corte
    // - todo el interés calculado queda como causado
    // - no genera contingente
    //
    // VENCIDOS (categoría B o superior):
    // - garantía personal (tipo_garantia = P):
    //      fecha inicial = ultima_fecha_interes
    // - otras garantías:
    //      cuota 2 = ultima_fecha_interes
    //      otra    = intereses_pagados_hasta
    // - fecha final = menor(fecha_final, fecha_corte)
    //
    // DISTRIBUCIÓN CONTINGENTE:
    // - categoría < C: todo causado
    // - categoría >= C y clasificación S/M/P:
    //      máximo causado = 60 días
    // - categoría >= C y clasificación C:
    //      máximo causado = 90 días
    //
    // Convención de días: 30 / 360.
    // Redondeo: ROUND numeric de PostgreSQL.
    // =========================================================

    public int causarIntereses(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
        WITH base_previa AS
        (
            SELECT
                f.id_cartera_credito,
                cc.fecha_corte,

                f.ultima_fecha_interes,
                f.intereses_pagados_hasta,
                f.fecha_final,

                f.codigo_tipo_cuota,
                f.tipo_garantia,

                f.saldo_actual AS saldo_base,
                f.tasa_nominal_anual AS tasa_interes,
                f.codigo_clasificacion_credito,

                COALESCE(
                    r.edad_de_riesgo,
                    r.edad_de_mora,
                    'A'
                ) AS categoria

            FROM cartera.cierres_cartera_creditos f

            INNER JOIN cartera.cierres_cartera cc
                ON cc.id_cierre_cartera =
                   f.id_cierre_cartera

            INNER JOIN cartera.cierres_cartera_resultados r
                ON r.id_cierre_cartera_credito =
                   f.id_cierre_cartera_credito

               AND r.id_cierre_cartera =
                   f.id_cierre_cartera

            WHERE f.id_cierre_cartera =
                  :idCierreCartera

              AND f.saldo_actual > 0
        ),

        base AS
        (
            SELECT
                b.*,

                CASE
                    -- =========================================
                    -- VIGENTES: PROCESO_CUATRO_ESPECIAL_1_CORRECTO
                    -- =========================================
                    WHEN b.categoria = 'A'
                    THEN b.ultima_fecha_interes

                    -- =========================================
                    -- VENCIDOS - GARANTÍA PERSONAL (GTIA = 1)
                    -- =========================================
                    WHEN UPPER(
                             TRIM(
                                 COALESCE(
                                     b.tipo_garantia,
                                     ''
                                 )
                             )
                         ) = 'P'
                    THEN b.ultima_fecha_interes

                    -- =========================================
                    -- VENCIDOS - OTRAS GARANTÍAS (GTIA <> 1)
                    -- CUOTAF = 2 -> ULTFI
                    -- otra cuota -> INTFH
                    -- =========================================
                    WHEN b.codigo_tipo_cuota = '2'
                    THEN b.ultima_fecha_interes

                    ELSE b.intereses_pagados_hasta
                END AS fecha_inicial_periodo,

                CASE
                    -- Vigentes: el VB calcula hasta el corte.
                    WHEN b.categoria = 'A'
                    THEN b.fecha_corte

                    -- Vencidos: menor entre vencimiento y corte.
                    WHEN b.fecha_final IS NULL
                    THEN b.fecha_corte

                    WHEN b.fecha_final < b.fecha_corte
                    THEN b.fecha_final

                    ELSE b.fecha_corte
                END AS fecha_final_periodo

            FROM base_previa b
        ),

        dias AS
        (
            SELECT
                b.*,

                CASE
                    WHEN b.fecha_inicial_periodo IS NULL
                    THEN 0

                    WHEN b.fecha_inicial_periodo >=
                         b.fecha_final_periodo
                    THEN 0

                    ELSE GREATEST(
                        (
                            (
                                EXTRACT(
                                    YEAR
                                    FROM b.fecha_final_periodo
                                )::integer
                                -
                                EXTRACT(
                                    YEAR
                                    FROM b.fecha_inicial_periodo
                                )::integer
                            ) * 360
                        )
                        +
                        (
                            (
                                EXTRACT(
                                    MONTH
                                    FROM b.fecha_final_periodo
                                )::integer
                                -
                                EXTRACT(
                                    MONTH
                                    FROM b.fecha_inicial_periodo
                                )::integer
                            ) * 30
                        )
                        +
                        (
                            LEAST(
                                EXTRACT(
                                    DAY
                                    FROM b.fecha_final_periodo
                                )::integer,
                                30
                            )
                            -
                            LEAST(
                                EXTRACT(
                                    DAY
                                    FROM b.fecha_inicial_periodo
                                )::integer,
                                30
                            )
                        ),
                        0
                    )
                END AS dias_causados

            FROM base b
        ),

        calculo AS
        (
            SELECT
                d.*,

                ROUND(
                    (
                        d.dias_causados
                        *
                        d.tasa_interes
                        *
                        d.saldo_base
                    )
                    / 36000.0,
                    2
                ) AS interes_bruto,

                100.0000::numeric
                    AS porcentaje_aplicacion

            FROM dias d
        ),

        neto AS
        (
            SELECT
                c.*,

                ROUND(
                    c.interes_bruto
                    *
                    (
                        c.porcentaje_aplicacion
                        / 100.0
                    ),
                    0
                ) AS interes_neto,

                CASE
                    WHEN c.dias_causados > 0
                    THEN ROUND(
                        (
                            c.interes_bruto
                            *
                            (
                                c.porcentaje_aplicacion
                                / 100.0
                            )
                        )
                        /
                        c.dias_causados,
                        0
                    )

                    ELSE 0
                END AS interes_diario

            FROM calculo c
        ),

        distribucion AS
        (
            SELECT
                n.*,

                CASE
                    -- Vigentes: todo el interés va a causado.
                    WHEN n.categoria = 'A'
                    THEN ROUND(n.interes_neto, 0)

                    -- Vencidos B: todavía no hay contingencia.
                    WHEN n.categoria < 'C'
                    THEN ROUND(n.interes_neto, 0)

                    -- Consumo / micro / productivo: 60 días.
                    WHEN n.codigo_clasificacion_credito
                         IN ('S', 'M', 'P')
                         AND n.dias_causados > 60
                    THEN ROUND(
                        60 * n.interes_diario,
                        0
                    )

                    -- Comercial: 90 días.
                    WHEN n.codigo_clasificacion_credito = 'C'
                         AND n.dias_causados > 90
                    THEN ROUND(
                        90 * n.interes_diario,
                        0
                    )

                    ELSE ROUND(n.interes_neto, 0)

                END AS interes_causado

            FROM neto n
        )

        INSERT INTO cartera.creditos_intereses_causados
        (
            id_cartera_credito,
            fecha_movimiento,
            fecha_inicial_periodo,
            fecha_final_periodo,
            saldo_base,
            tasa_interes,
            dias_causados,
            interes_bruto,
            porcentaje_aplicacion,
            valor_debito,
            valor_credito,
            observacion_interes,
            fk_seguridad_creacion,
            fk_seguridad_edicion
        )

        SELECT
            d.id_cartera_credito,
            d.fecha_corte,
            d.fecha_inicial_periodo,
            d.fecha_final_periodo,
            d.saldo_base,
            d.tasa_interes,
            d.dias_causados,
            d.interes_bruto,
            d.porcentaje_aplicacion,
            d.interes_causado,
            0,
            :observacion,
            :idUsuario,
            :idUsuario

        FROM distribucion d
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
                        )
                        .addValue(
                                "observacion",
                                OBS_CAUSADOS
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // CAUSAR INTERESES CONTINGENTES
    //
    // Usa exactamente la misma base de cálculo y las mismas
    // fechas de causarIntereses().
    //
    // El contingente es:
    //      interés neto total - interés reconocido como causado
    //
    // Para dejar trazabilidad del cierre se inserta un registro
    // por crédito, incluso cuando el valor contingente sea cero.
    // =========================================================

    public int causarInteresesContingentes(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
        WITH base_previa AS
        (
            SELECT
                f.id_cartera_credito,
                cc.fecha_corte,

                f.ultima_fecha_interes,
                f.intereses_pagados_hasta,
                f.fecha_final,

                f.codigo_tipo_cuota,
                f.tipo_garantia,

                f.saldo_actual AS saldo_base,
                f.tasa_nominal_anual AS tasa_interes,
                f.codigo_clasificacion_credito,

                COALESCE(
                    r.edad_de_riesgo,
                    r.edad_de_mora,
                    'A'
                ) AS categoria

            FROM cartera.cierres_cartera_creditos f

            INNER JOIN cartera.cierres_cartera cc
                ON cc.id_cierre_cartera =
                   f.id_cierre_cartera

            INNER JOIN cartera.cierres_cartera_resultados r
                ON r.id_cierre_cartera_credito =
                   f.id_cierre_cartera_credito

               AND r.id_cierre_cartera =
                   f.id_cierre_cartera

            WHERE f.id_cierre_cartera =
                  :idCierreCartera

              AND f.saldo_actual > 0
        ),

        base AS
        (
            SELECT
                b.*,

                CASE
                    WHEN b.categoria = 'A'
                    THEN b.ultima_fecha_interes

                    WHEN UPPER(
                             TRIM(
                                 COALESCE(
                                     b.tipo_garantia,
                                     ''
                                 )
                             )
                         ) = 'P'
                    THEN b.ultima_fecha_interes

                    WHEN b.codigo_tipo_cuota = '2'
                    THEN b.ultima_fecha_interes

                    ELSE b.intereses_pagados_hasta
                END AS fecha_inicial_periodo,

                CASE
                    WHEN b.categoria = 'A'
                    THEN b.fecha_corte

                    WHEN b.fecha_final IS NULL
                    THEN b.fecha_corte

                    WHEN b.fecha_final < b.fecha_corte
                    THEN b.fecha_final

                    ELSE b.fecha_corte
                END AS fecha_final_periodo

            FROM base_previa b
        ),

        dias AS
        (
            SELECT
                b.*,

                CASE
                    WHEN b.fecha_inicial_periodo IS NULL
                    THEN 0

                    WHEN b.fecha_inicial_periodo >=
                         b.fecha_final_periodo
                    THEN 0

                    ELSE GREATEST(
                        (
                            (
                                EXTRACT(
                                    YEAR
                                    FROM b.fecha_final_periodo
                                )::integer
                                -
                                EXTRACT(
                                    YEAR
                                    FROM b.fecha_inicial_periodo
                                )::integer
                            ) * 360
                        )
                        +
                        (
                            (
                                EXTRACT(
                                    MONTH
                                    FROM b.fecha_final_periodo
                                )::integer
                                -
                                EXTRACT(
                                    MONTH
                                    FROM b.fecha_inicial_periodo
                                )::integer
                            ) * 30
                        )
                        +
                        (
                            LEAST(
                                EXTRACT(
                                    DAY
                                    FROM b.fecha_final_periodo
                                )::integer,
                                30
                            )
                            -
                            LEAST(
                                EXTRACT(
                                    DAY
                                    FROM b.fecha_inicial_periodo
                                )::integer,
                                30
                            )
                        ),
                        0
                    )
                END AS dias_causados

            FROM base b
        ),

        calculo AS
        (
            SELECT
                d.*,

                ROUND(
                    (
                        d.dias_causados
                        *
                        d.tasa_interes
                        *
                        d.saldo_base
                    )
                    / 36000.0,
                    2
                ) AS interes_bruto,

                100.0000::numeric
                    AS porcentaje_aplicacion

            FROM dias d
        ),

        neto AS
        (
            SELECT
                c.*,

                ROUND(
                    c.interes_bruto
                    *
                    (
                        c.porcentaje_aplicacion
                        / 100.0
                    ),
                    0
                ) AS interes_neto,

                CASE
                    WHEN c.dias_causados > 0
                    THEN ROUND(
                        (
                            c.interes_bruto
                            *
                            (
                                c.porcentaje_aplicacion
                                / 100.0
                            )
                        )
                        /
                        c.dias_causados,
                        0
                    )

                    ELSE 0
                END AS interes_diario

            FROM calculo c
        ),

        distribucion AS
        (
            SELECT
                n.*,

                CASE
                    WHEN n.categoria = 'A'
                    THEN n.interes_neto

                    WHEN n.categoria < 'C'
                    THEN n.interes_neto

                    WHEN n.codigo_clasificacion_credito
                         IN ('S', 'M', 'P')
                         AND n.dias_causados > 60
                    THEN ROUND(
                        60 * n.interes_diario,
                        0
                    )

                    WHEN n.codigo_clasificacion_credito = 'C'
                         AND n.dias_causados > 90
                    THEN ROUND(
                        90 * n.interes_diario,
                        0
                    )

                    ELSE n.interes_neto

                END AS interes_causado

            FROM neto n
        ),

        contingente AS
        (
            SELECT
                d.*,

                CASE
                    -- Los vigentes nunca generan contingente.
                    WHEN d.categoria = 'A'
                    THEN 0

                    ELSE GREATEST(
                        ROUND(
                            d.interes_neto
                            -
                            d.interes_causado,
                            0
                        ),
                        0
                    )
                END AS interes_contingente

            FROM distribucion d
        )

        INSERT INTO cartera.creditos_intereses_contingentes
        (
            id_cartera_credito,
            fecha_movimiento,
            fecha_inicial_periodo,
            fecha_final_periodo,
            saldo_base,
            tasa_interes,
            dias_causados,
            interes_bruto,
            porcentaje_aplicacion,
            valor_debito,
            valor_credito,
            observacion_interes,
            fk_seguridad_creacion,
            fk_seguridad_edicion
        )

        SELECT
            c.id_cartera_credito,
            c.fecha_corte,
            c.fecha_inicial_periodo,
            c.fecha_final_periodo,
            c.saldo_base,
            c.tasa_interes,
            c.dias_causados,
            c.interes_bruto,
            c.porcentaje_aplicacion,
            c.interes_contingente,
            0,
            :observacion,
            :idUsuario,
            :idUsuario

        FROM contingente c
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
                        )
                        .addValue(
                                "observacion",
                                OBS_CONTINGENTES
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // CONSOLIDAR RESULTADOS DE INTERESES DEL CIERRE
    //
    // GUARDA POR CADA CRÉDITO:
    //
    // 1. valor_intereses_causados_mes
    //    = débitos originados por causación durante el mes
    //
    // 2. saldo_intereses_causados
    //    = débitos - créditos acumulados hasta fecha_corte
    //
    // 3. valor_intereses_contingentes_mes
    //    = débitos contingentes causados durante el mes
    //
    // 4. saldo_intereses_contingentes
    //    = débitos - créditos contingentes hasta fecha_corte
    //
    // IMPORTANTE:
    //
    // Los valores DEL MES identifican movimientos de causación
    // porque tienen información técnica del cálculo:
    // fecha_inicial_periodo + fecha_final_periodo + interes_bruto.
    //
    // De esta manera una anulación de pago registrada al débito
    // NO se confunde con una causación del mes.
    // =========================================================

    public int consolidarResultadosIntereses(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
        WITH cierre AS
        (
            SELECT
                c.id_cierre_cartera,
                c.fecha_corte,

                date_trunc(
                    'month',
                    c.fecha_corte
                )::date AS fecha_inicio_mes

            FROM cartera.cierres_cartera c

            WHERE c.id_cierre_cartera =
                  :idCierreCartera
        ),

        base_creditos AS
        (
            SELECT
                f.id_cierre_cartera_credito,
                f.id_cartera_credito

            FROM cartera.cierres_cartera_creditos f

            WHERE f.id_cierre_cartera =
                  :idCierreCartera
        ),

        causados AS
        (
            SELECT
                bc.id_cierre_cartera_credito,

                -- =============================================
                -- CAUSADO DURANTE EL MES
                -- =============================================

                COALESCE(
                    SUM(
                        CASE

                            WHEN ic.fecha_movimiento
                                 BETWEEN c.fecha_inicio_mes
                                     AND c.fecha_corte

                             AND ic.fecha_inicial_periodo
                                 IS NOT NULL

                             AND ic.fecha_final_periodo
                                 IS NOT NULL

                             AND ic.interes_bruto
                                 IS NOT NULL

                            THEN ic.valor_debito

                            ELSE 0

                        END
                    ),
                    0
                ) AS valor_intereses_causados_mes,

                -- =============================================
                -- SALDO CAUSADO AL CORTE
                -- =============================================

                COALESCE(
                    SUM(
                        CASE

                            WHEN ic.fecha_movimiento
                                 <= c.fecha_corte

                            THEN
                                ic.valor_debito
                                -
                                ic.valor_credito

                            ELSE 0

                        END
                    ),
                    0
                ) AS saldo_intereses_causados

            FROM base_creditos bc

            CROSS JOIN cierre c

            LEFT JOIN cartera.creditos_intereses_causados ic
                ON ic.id_cartera_credito =
                   bc.id_cartera_credito

            GROUP BY
                bc.id_cierre_cartera_credito
        ),

        contingentes AS
        (
            SELECT
                bc.id_cierre_cartera_credito,

                -- =============================================
                -- CONTINGENTE CAUSADO DURANTE EL MES
                -- =============================================

                COALESCE(
                    SUM(
                        CASE

                            WHEN ict.fecha_movimiento
                                 BETWEEN c.fecha_inicio_mes
                                     AND c.fecha_corte

                             AND ict.fecha_inicial_periodo
                                 IS NOT NULL

                             AND ict.fecha_final_periodo
                                 IS NOT NULL

                             AND ict.interes_bruto
                                 IS NOT NULL

                            THEN ict.valor_debito

                            ELSE 0

                        END
                    ),
                    0
                ) AS valor_intereses_contingentes_mes,

                -- =============================================
                -- SALDO CONTINGENTE AL CORTE
                -- =============================================

                COALESCE(
                    SUM(
                        CASE

                            WHEN ict.fecha_movimiento
                                 <= c.fecha_corte

                            THEN
                                ict.valor_debito
                                -
                                ict.valor_credito

                            ELSE 0

                        END
                    ),
                    0
                ) AS saldo_intereses_contingentes

            FROM base_creditos bc

            CROSS JOIN cierre c

            LEFT JOIN cartera.creditos_intereses_contingentes ict
                ON ict.id_cartera_credito =
                   bc.id_cartera_credito

            GROUP BY
                bc.id_cierre_cartera_credito
        )

        UPDATE cartera.cierres_cartera_resultados r

           SET valor_intereses_causados_mes =
                   GREATEST(
                       ca.valor_intereses_causados_mes,
                       0
                   ),

               saldo_intereses_causados =
                   GREATEST(
                       ca.saldo_intereses_causados,
                       0
                   ),

               valor_intereses_contingentes_mes =
                   GREATEST(
                       co.valor_intereses_contingentes_mes,
                       0
                   ),

               saldo_intereses_contingentes =
                   GREATEST(
                       co.saldo_intereses_contingentes,
                       0
                   ),

               fecha_calculo =
                   CURRENT_TIMESTAMP,

               fk_seguridad_edicion =
                   :idUsuario,

               fecha_edicion =
                   CURRENT_TIMESTAMP

          FROM causados ca

          INNER JOIN contingentes co
              ON co.id_cierre_cartera_credito =
                 ca.id_cierre_cartera_credito

         WHERE r.id_cierre_cartera_credito =
               ca.id_cierre_cartera_credito

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
