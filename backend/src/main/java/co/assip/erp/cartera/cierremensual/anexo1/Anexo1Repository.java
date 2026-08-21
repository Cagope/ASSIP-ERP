package co.assip.erp.cartera.cierremensual.anexo1;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class Anexo1Repository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // CALCULAR EDAD CONTABLE POR ALINEAMIENTO / ARRASTRE
    //
    // REGLA ANEXO 1:
    //
    // 1. La edad_de_riesgo ya debe venir consolidada después de:
    //
    //    - mora
    //    - regla una sola cuota
    //    - evaluación
    //    - reestructuración
    //
    // 2. Se agrupa por:
    //
    //    - id_datos_personal
    //    - codigo_clasificacion_credito
    //
    // 3. Para cada grupo se toma la peor edad_de_riesgo.
    //
    // 4. Esa edad final se guarda en:
    //
    //    edad_contable
    //
    // ORDEN:
    //    A = 1
    //    B = 2
    //    C = 3
    //    D = 4
    //    E = 5
    // =========================================================

    public int calcularEdadContablePorArrastre(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
            WITH base AS
            (
                SELECT
                    r.id_cierre_cartera_resultado,
                    f.id_datos_personal,
                    f.codigo_clasificacion_credito,

                    CASE UPPER(
                        TRIM(
                            COALESCE(
                                r.edad_de_riesgo,
                                'A'
                            )
                        )
                    )
                        WHEN 'A' THEN 1
                        WHEN 'B' THEN 2
                        WHEN 'C' THEN 3
                        WHEN 'D' THEN 4
                        WHEN 'E' THEN 5
                        ELSE 1
                    END AS orden_riesgo

                FROM cartera.cierres_cartera_resultados r

                INNER JOIN cartera.cierres_cartera_creditos f
                    ON f.id_cierre_cartera_credito =
                       r.id_cierre_cartera_credito

                WHERE r.id_cierre_cartera =
                      :idCierreCartera
            ),

            alineamiento AS
            (
                SELECT
                    b.id_cierre_cartera_resultado,

                    MAX(
                        b.orden_riesgo
                    ) OVER
                    (
                        PARTITION BY
                            b.id_datos_personal,
                            b.codigo_clasificacion_credito
                    ) AS orden_contable

                FROM base b
            ),

            calculada AS
            (
                SELECT
                    a.id_cierre_cartera_resultado,

                    CASE a.orden_contable
                        WHEN 1 THEN 'A'
                        WHEN 2 THEN 'B'
                        WHEN 3 THEN 'C'
                        WHEN 4 THEN 'D'
                        WHEN 5 THEN 'E'
                        ELSE 'A'
                    END AS edad_contable

                FROM alineamiento a
            )

            UPDATE cartera.cierres_cartera_resultados r

               SET edad_contable =
                       c.edad_contable,

                   fk_seguridad_edicion =
                       :idUsuario,

                   fecha_edicion =
                       CURRENT_TIMESTAMP

              FROM calculada c

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
    // CALCULAR DETERIORO DE CAPITAL - ANEXO 1
    //
    // REGLA:
    //
    // 1. Se toma la edad_contable final.
    //
    // 2. Se identifica el tipo de persona desde la fotografía
    //    de hoja de vida correspondiente a la fecha de corte.
    //
    // 3. Se obtiene el porcentaje normal de deterioro desde:
    //
    //       cartera.porcentajes_deterioro
    //
    //    usando:
    //
    //       - codigo_clasificacion_credito
    //       - tipo_persona
    //       - edad_contable
    //       - vigencia
    //
    // 4. Se busca una regla especial desde:
    //
    //       cartera.porcentajes_deterioro_especial
    //
    //    usando:
    //
    //       - codigo_clasificacion_credito
    //       - tipo_persona
    //       - edad_de_mora
    //       - dias_mora
    //       - vigencia
    //
    //    Si existe regla especial, su porcentaje tiene
    //    precedencia sobre el porcentaje normal.
    //
    //    E1 / E2 NO reemplazan edad_contable.
    //
    // 5. Se determina el porcentaje reconocido de garantía
    //    según:
    //
    //       - codigo_garantia_credito
    //       - dias_mora
    //       - vigencia a fecha de corte
    //
    // 6. Valor reconocido de garantía:
    //
    //       valor_garantias_credito
    //       * porcentaje_aplicacion
    //       / 100
    //
    // 7. Base de deterioro:
    //
    //       saldo_actual
    //       - valor_aportes_credito
    //       - valor_garantia_reconocida
    //
    //    con piso en cero.
    //
    // 8. deterioro_capital:
    //
    //       base
    //       * porcentaje_deterioro_capital
    //       / 100
    //
    // =========================================================

    public int calcularDeterioroCapital(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
            WITH base AS
            (
                SELECT
                    r.id_cierre_cartera_resultado,

                    COALESCE(
                        r.saldo_actual,
                        0
                    ) AS saldo_actual,

                    COALESCE(
                        r.valor_aportes_credito,
                        0
                    ) AS valor_aportes_credito,

                    COALESCE(
                        r.valor_garantias_credito,
                        0
                    ) AS valor_garantias_credito,

                    COALESCE(
                        pg.porcentaje_aplicacion,
                        0
                    ) AS porcentaje_aplicacion_garantia,

                    COALESCE(
                        pde.porcentaje_deterioro_capital,
                        pd.porcentaje_deterioro_capital
                    ) AS porcentaje_deterioro_capital

                FROM cartera.cierres_cartera_resultados r

                INNER JOIN cartera.cierres_cartera_creditos f
                    ON f.id_cierre_cartera_credito =
                       r.id_cierre_cartera_credito

                INNER JOIN cartera.cierres_cartera c
                    ON c.id_cierre_cartera =
                       r.id_cierre_cartera

                INNER JOIN hoja_vida.cierres_hoja_vida chv
                    ON chv.fecha_corte =
                       c.fecha_corte

                INNER JOIN hoja_vida.cierres_hoja_vida_personas hv
                    ON hv.id_cierre_hoja_vida =
                       chv.id_cierre_hoja_vida

                   AND hv.id_datos_personal =
                       f.id_datos_personal

                /*
                 * PORCENTAJE NORMAL
                 *
                 * Se determina con la edad contable final.
                 */
                INNER JOIN cartera.porcentajes_deterioro pd
                    ON pd.codigo_clasificacion_credito =
                       f.codigo_clasificacion_credito

                   AND pd.id_tipo_persona =
                       TRIM(hv.tipo_persona)::integer

                   AND pd.codigo_clasificacion_deterioro =
                       r.edad_contable

                   AND pd.activo = true

                   AND pd.vigencia_desde <=
                       c.fecha_corte

                   AND (
                        pd.vigencia_hasta IS NULL
                        OR pd.vigencia_hasta >=
                           c.fecha_corte
                   )

                /*
                 * PORCENTAJE ESPECIAL
                 *
                 * Se determina con:
                 *
                 * - clasificación del crédito
                 * - tipo de persona
                 * - edad de mora
                 * - rango de días de mora
                 *
                 * Si existe, tiene precedencia sobre el normal.
                 *
                 * LATERAL + LIMIT 1 evita multiplicar el crédito
                 * ante una eventual parametrización solapada.
                 */
                LEFT JOIN LATERAL
                (
                    SELECT
                        pe.porcentaje_deterioro_capital

                    FROM cartera.porcentajes_deterioro_especial pe

                    WHERE pe.codigo_clasificacion_credito =
                          f.codigo_clasificacion_credito

                      AND pe.id_tipo_persona =
                          TRIM(hv.tipo_persona)::smallint

                      AND pe.codigo_edad_mora =
                          r.edad_de_mora

                      AND r.dias_mora BETWEEN
                          pe.dias_desde
                          AND pe.dias_hasta

                      AND pe.activo = true

                      AND pe.vigencia_desde <=
                          c.fecha_corte

                      AND (
                           pe.vigencia_hasta IS NULL
                           OR pe.vigencia_hasta >=
                              c.fecha_corte
                      )

                    ORDER BY
                        pe.dias_desde DESC,
                        pe.id_porcentaje_deterioro_especial DESC

                    LIMIT 1

                ) pde ON true

                /*
                 * PORCENTAJE DE RECONOCIMIENTO DE GARANTÍA
                 */
                LEFT JOIN LATERAL
                (
                    SELECT
                        p.porcentaje_aplicacion

                    FROM cartera.porcentajes_aplicacion_garantia p

                    WHERE p.codigo_garantia_credito =
                          f.codigo_garantia_credito

                      AND r.dias_mora <=
                          p.dias_hasta

                      AND p.activo = true

                      AND p.vigencia_desde <=
                          c.fecha_corte

                      AND (
                           p.vigencia_hasta IS NULL
                           OR p.vigencia_hasta >=
                              c.fecha_corte
                      )

                    ORDER BY
                        p.dias_hasta

                    LIMIT 1

                ) pg ON true

                WHERE r.id_cierre_cartera =
                      :idCierreCartera
            ),

            calculada AS
            (
                SELECT
                    b.id_cierre_cartera_resultado,

                    ROUND(
                        GREATEST(
                            b.saldo_actual
                            -
                            b.valor_aportes_credito
                            -
                            ROUND(
                                b.valor_garantias_credito
                                *
                                b.porcentaje_aplicacion_garantia
                                / 100,
                                0
                            ),
                            0
                        )
                        *
                        b.porcentaje_deterioro_capital
                        / 100,
                        0
                    ) AS deterioro_capital

                FROM base b
            )

            UPDATE cartera.cierres_cartera_resultados r

               SET deterioro_capital =
                       c.deterioro_capital,

                   fk_seguridad_edicion =
                       :idUsuario,

                   fecha_edicion =
                       CURRENT_TIMESTAMP

              FROM calculada c

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
    // CALCULAR DETERIORO DE INTERESES - ANEXO 1
    //
    // REGLA:
    //
    // 1. Se obtiene el porcentaje normal según:
    //
    //       - clasificación del crédito
    //       - tipo de persona
    //       - edad_contable
    //
    // 2. Se busca porcentaje especial según:
    //
    //       - clasificación del crédito
    //       - tipo de persona
    //       - edad_de_mora
    //       - dias_mora
    //
    // 3. Si existe porcentaje especial, tiene precedencia.
    //
    // 4. deterioro_intereses:
    //
    //       saldo_intereses_causados
    //       * porcentaje_deterioro_intereses
    //       / 100
    //
    // La garantía NO reduce la base de intereses.
    //
    // E1 / E2 NO modifican edad_contable.
    // =========================================================

    public int calcularDeterioroIntereses(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
            WITH base AS
            (
                SELECT
                    r.id_cierre_cartera_resultado,

                    COALESCE(
                        r.saldo_intereses_causados,
                        0
                    ) AS saldo_intereses_causados,

                    COALESCE(
                        pde.porcentaje_deterioro_intereses,
                        pd.porcentaje_deterioro_intereses
                    ) AS porcentaje_deterioro_intereses

                FROM cartera.cierres_cartera_resultados r

                INNER JOIN cartera.cierres_cartera_creditos f
                    ON f.id_cierre_cartera_credito =
                       r.id_cierre_cartera_credito

                INNER JOIN cartera.cierres_cartera c
                    ON c.id_cierre_cartera =
                       r.id_cierre_cartera

                INNER JOIN hoja_vida.cierres_hoja_vida chv
                    ON chv.fecha_corte =
                       c.fecha_corte

                INNER JOIN hoja_vida.cierres_hoja_vida_personas hv
                    ON hv.id_cierre_hoja_vida =
                       chv.id_cierre_hoja_vida

                   AND hv.id_datos_personal =
                       f.id_datos_personal

                /*
                 * PORCENTAJE NORMAL
                 */
                INNER JOIN cartera.porcentajes_deterioro pd
                    ON pd.codigo_clasificacion_credito =
                       f.codigo_clasificacion_credito

                   AND pd.id_tipo_persona =
                       TRIM(hv.tipo_persona)::integer

                   AND pd.codigo_clasificacion_deterioro =
                       r.edad_contable

                   AND pd.activo = true

                   AND pd.vigencia_desde <=
                       c.fecha_corte

                   AND (
                        pd.vigencia_hasta IS NULL
                        OR pd.vigencia_hasta >=
                           c.fecha_corte
                   )

                /*
                 * PORCENTAJE ESPECIAL
                 *
                 * Si existe, tiene precedencia sobre el normal.
                 */
                LEFT JOIN LATERAL
                (
                    SELECT
                        pe.porcentaje_deterioro_intereses

                    FROM cartera.porcentajes_deterioro_especial pe

                    WHERE pe.codigo_clasificacion_credito =
                          f.codigo_clasificacion_credito

                      AND pe.id_tipo_persona =
                          TRIM(hv.tipo_persona)::smallint

                      AND pe.codigo_edad_mora =
                          r.edad_de_mora

                      AND r.dias_mora BETWEEN
                          pe.dias_desde
                          AND pe.dias_hasta

                      AND pe.activo = true

                      AND pe.vigencia_desde <=
                          c.fecha_corte

                      AND (
                           pe.vigencia_hasta IS NULL
                           OR pe.vigencia_hasta >=
                              c.fecha_corte
                      )

                    ORDER BY
                        pe.dias_desde DESC,
                        pe.id_porcentaje_deterioro_especial DESC

                    LIMIT 1

                ) pde ON true

                WHERE r.id_cierre_cartera =
                      :idCierreCartera
            ),

            calculada AS
            (
                SELECT
                    b.id_cierre_cartera_resultado,

                    ROUND(
                        b.saldo_intereses_causados
                        *
                        b.porcentaje_deterioro_intereses
                        / 100,
                        0
                    ) AS deterioro_intereses

                FROM base b
            )

            UPDATE cartera.cierres_cartera_resultados r

               SET deterioro_intereses =
                       c.deterioro_intereses,

                   fk_seguridad_edicion =
                       :idUsuario,

                   fecha_edicion =
                       CURRENT_TIMESTAMP

              FROM calculada c

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