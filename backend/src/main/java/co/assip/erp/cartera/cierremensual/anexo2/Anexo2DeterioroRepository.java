package co.assip.erp.cartera.cierremensual.anexo2;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class Anexo2DeterioroRepository {

    private final JdbcTemplate jdbcTemplate;

    // =========================================================
    // CALCULAR VEA
    //
    // VEA = VALOR EXPUESTO DEL ACTIVO
    //
    // Fórmula actualmente implementada:
    //
    // saldo capital
    // + intereses causados
    // + costas judiciales
    // - aportes reconocidos
    //
    // Crédito desembolsado durante el mismo mes del cierre:
    //
    // VEA = 0
    //
    // =========================================================

    public int calcularVea(
            Integer idCierreCartera
    ) {

        // =====================================================
        // 1. CALCULAR Y CONGELAR BASES DEL VEA
        // =====================================================

        String sql = """
            UPDATE tmp_pe_variables v

            SET
                -- =============================================
                -- INDICADOR CRÉDITO DESEMBOLSADO EN EL MES
                -- =============================================

                tipo_doc_pe =
                    CASE
                        WHEN EXTRACT(
                                YEAR FROM v.fecha_desembolso
                             ) =
                             EXTRACT(
                                YEAR FROM c.fecha_corte
                             )

                         AND EXTRACT(
                                MONTH FROM v.fecha_desembolso
                             ) =
                             EXTRACT(
                                MONTH FROM c.fecha_corte
                             )

                            THEN 1

                        ELSE 0
                    END,

                -- =============================================
                -- BASE CAPITAL
                -- =============================================

                base_vea_capital =
                    COALESCE(
                        v.saldo_actual,
                        0
                    ),

                -- =============================================
                -- BASE INTERESES
                -- =============================================

                base_vea_intereses =
                    COALESCE(
                        v.saldo_intereses_causados,
                        0
                    ),

                -- =============================================
                -- BASE COSTAS JUDICIALES
                -- =============================================

                base_vea_costas_judiciales =
                    COALESCE(
                        v.valor_costas_judiciales,
                        0
                    ),

                -- =============================================
                -- OTROS
                --
                -- Actualmente no participan en la fórmula VEA.
                -- =============================================

                base_vea_otros =
                    0,

                -- =============================================
                -- APORTES RECONOCIDOS AL CRÉDITO
                -- =============================================

                base_vea_aportes =
                    COALESCE(
                        v.valor_aportes_credito,
                        0
                    ),

                -- =============================================
                -- AHORRO PERMANENTE
                --
                -- Todavía no implementado.
                -- =============================================

                base_vea_ahorro_permanente =
                    0,

                -- =============================================
                -- VEA BRUTO
                -- =============================================

                vea_bruto =
                      COALESCE(
                          v.saldo_actual,
                          0
                      )
                    + COALESCE(
                          v.saldo_intereses_causados,
                          0
                      )
                    + COALESCE(
                          v.valor_costas_judiciales,
                          0
                      ),

                -- =============================================
                -- DEDUCCIONES VEA
                -- =============================================

                vea_deducciones =
                    COALESCE(
                        v.valor_aportes_credito,
                        0
                    ),

                -- =============================================
                -- VEA FINAL
                -- =============================================

                vea =
                    CASE
                        WHEN EXTRACT(
                                YEAR FROM v.fecha_desembolso
                             ) =
                             EXTRACT(
                                YEAR FROM c.fecha_corte
                             )

                         AND EXTRACT(
                                MONTH FROM v.fecha_desembolso
                             ) =
                             EXTRACT(
                                MONTH FROM c.fecha_corte
                             )

                            THEN 0

                        ELSE GREATEST(
                            0,

                              COALESCE(
                                  v.saldo_actual,
                                  0
                              )

                            + COALESCE(
                                  v.saldo_intereses_causados,
                                  0
                              )

                            + COALESCE(
                                  v.valor_costas_judiciales,
                                  0
                              )

                            - COALESCE(
                                  v.valor_aportes_credito,
                                  0
                              )
                        )
                    END

            FROM cartera.cierres_cartera c

            WHERE c.id_cierre_cartera = ?
            """;

        int actualizados =
                jdbcTemplate.update(
                        sql,
                        idCierreCartera
                );

        // =====================================================
        // 2. VALIDAR RESULTADOS INCOMPLETOS
        // =====================================================

        Integer incompletos =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)

                        FROM tmp_pe_variables

                        WHERE tipo_doc_pe IS NULL

                           OR base_vea_capital IS NULL
                           OR base_vea_intereses IS NULL
                           OR base_vea_costas_judiciales IS NULL
                           OR base_vea_otros IS NULL
                           OR base_vea_aportes IS NULL
                           OR base_vea_ahorro_permanente IS NULL

                           OR vea_bruto IS NULL
                           OR vea_deducciones IS NULL
                           OR vea IS NULL
                        """,
                        Integer.class
                );

        if (incompletos != null
                && incompletos > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + incompletos
                            + " créditos con cálculo VEA incompleto."
            );
        }

        // =====================================================
        // 3. VALIDAR VALORES NEGATIVOS
        // =====================================================

        Integer negativos =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)

                        FROM tmp_pe_variables

                        WHERE base_vea_capital < 0
                           OR base_vea_intereses < 0
                           OR base_vea_costas_judiciales < 0
                           OR base_vea_otros < 0
                           OR base_vea_aportes < 0
                           OR base_vea_ahorro_permanente < 0
                           OR vea_bruto < 0
                           OR vea_deducciones < 0
                           OR vea < 0
                        """,
                        Integer.class
                );

        if (negativos != null
                && negativos > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + negativos
                            + " créditos con bases VEA negativas."
            );
        }

        // =====================================================
        // 4. VALIDAR CANTIDAD ACTUALIZADA
        // =====================================================

        Integer cantidad =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM tmp_pe_variables
                        """,
                        Integer.class
                );

        int esperados =
                cantidad != null
                        ? cantidad
                        : 0;

        if (actualizados != esperados) {

            throw new IllegalStateException(
                    "Inconsistencia al calcular VEA. "
                            + "Créditos esperados: "
                            + esperados
                            + ". Créditos actualizados: "
                            + actualizados
                            + "."
            );
        }

        return actualizados;
    }


    // =========================================================
    // CALCULAR PDI Y PÉRDIDA ESPERADA
    //
    // ETAPAS:
    //
    // 1. Congelar garantía utilizada.
    // 2. Calcular PDI y tramo.
    // 3. Calcular pérdida esperada.
    // 4. Calcular bases de distribución.
    // 5. Calcular porcentajes.
    // 6. Distribuir pérdida:
    //      - capital
    //      - intereses
    //      - otros como remanente
    // 7. Consolidar deterioros PE.
    // 8. Validar consistencia.
    //
    // Fórmula:
    //
    // PÉRDIDA =
    //      (PI / 100)
    //    * VEA
    //    * (PDI / 100)
    //
    // =========================================================

    public int calcularPdiYPerdida() {

        // =====================================================
        // 1. CONGELAR INFORMACIÓN DE GARANTÍA
        //
        // Los valores monetarios de garantía ya fueron
        // calculados previamente en:
        //
        // cartera.cierres_cartera_resultados
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables v

                SET
                    codigo_garantia_pdi =
                        v.codigo_garantia_credito,

                    nombre_garantia_pdi =
                        (
                            SELECT
                                g.descripcion_garantia_credito

                            FROM cartera.garantias_creditos g

                            WHERE g.codigo_garantia_credito =
                                  v.codigo_garantia_credito

                            LIMIT 1
                        ),

                    valor_garantia =
                        COALESCE(
                            r.valor_garantias_total,
                            0
                        ),

                    porcentaje_garantia_reconocido =
                        COALESCE(
                            r.porcentaje_garantias_credito,
                            0
                        ),

                    valor_garantia_reconocido =
                        COALESCE(
                            r.valor_garantias_credito,
                            0
                        )

                FROM cartera.cierres_cartera_resultados r

                WHERE r.id_cierre_cartera_credito =
                      v.id_cierre_cartera_credito
                """
        );


        // =====================================================
        // 1.1 VALIDAR DESCRIPCIÓN DE GARANTÍA
        // =====================================================

        Integer garantiasSinDescripcion =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)

                        FROM tmp_pe_variables

                        WHERE codigo_garantia_pdi IS NOT NULL
                          AND nombre_garantia_pdi IS NULL
                        """,
                        Integer.class
                );

        if (garantiasSinDescripcion != null
                && garantiasSinDescripcion > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + garantiasSinDescripcion
                            + " créditos PE con código de garantía "
                            + "sin descripción en cartera.garantias_creditos."
            );
        }


        // =====================================================
        // 2. CALCULAR PDI Y CONGELAR TRAMO UTILIZADO
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables v

                SET
                    dias_mora_pdi =
                        v.dias_mora_actual,

                    tramo_pdi =
                        CASE
                            WHEN v.dias_mora_actual <=
                                 (p.mora_1 + p.dias_1)
                                THEN 1

                            WHEN v.dias_mora_actual <=
                                 (p.mora_2 + p.dias_2)
                                THEN 2

                            WHEN v.dias_mora_actual <=
                                 (p.mora_3 + p.dias_3)
                                THEN 3

                            ELSE 4
                        END,

                    dias_desde_pdi =
                        CASE
                            WHEN v.dias_mora_actual <=
                                 (p.mora_1 + p.dias_1)
                                THEN 0

                            WHEN v.dias_mora_actual <=
                                 (p.mora_2 + p.dias_2)
                                THEN
                                    (p.mora_1 + p.dias_1) + 1

                            WHEN v.dias_mora_actual <=
                                 (p.mora_3 + p.dias_3)
                                THEN
                                    (p.mora_2 + p.dias_2) + 1

                            ELSE
                                (p.mora_3 + p.dias_3) + 1
                        END,

                    dias_hasta_pdi =
                        CASE
                            WHEN v.dias_mora_actual <=
                                 (p.mora_1 + p.dias_1)
                                THEN
                                    p.mora_1 + p.dias_1

                            WHEN v.dias_mora_actual <=
                                 (p.mora_2 + p.dias_2)
                                THEN
                                    p.mora_2 + p.dias_2

                            WHEN v.dias_mora_actual <=
                                 (p.mora_3 + p.dias_3)
                                THEN
                                    p.mora_3 + p.dias_3

                            ELSE NULL
                        END,

                    pdi =
                        CASE
                            WHEN v.dias_mora_actual <=
                                 (p.mora_1 + p.dias_1)
                                THEN p.porcentaje_1

                            WHEN v.dias_mora_actual <=
                                 (p.mora_2 + p.dias_2)
                                THEN p.porcentaje_2

                            WHEN v.dias_mora_actual <=
                                 (p.mora_3 + p.dias_3)
                                THEN p.porcentaje_3

                            ELSE p.porcentaje_4
                        END

                FROM cartera.pe_modelos_pdi p

                WHERE p.id_modelo_pe =
                      v.id_modelo_pe

                  AND p.codigo_garantia_credito =
                      v.codigo_garantia_credito

                  AND p.activo = TRUE
                """
        );


        // =====================================================
        // 3. VALIDAR PDI
        // =====================================================

        Integer sinPdi =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)

                        FROM tmp_pe_variables

                        WHERE pdi IS NULL
                           OR tramo_pdi IS NULL
                           OR dias_mora_pdi IS NULL
                           OR dias_desde_pdi IS NULL
                        """,
                        Integer.class
                );

        if (sinPdi != null
                && sinPdi > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + sinPdi
                            + " créditos sin parametrización "
                            + "o trazabilidad PDI completa."
            );
        }


        // =====================================================
        // 4. VALIDAR RANGO PDI
        // =====================================================

        Integer pdiInvalidas =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)

                        FROM tmp_pe_variables

                        WHERE pdi < 0
                           OR pdi > 100
                        """,
                        Integer.class
                );

        if (pdiInvalidas != null
                && pdiInvalidas > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + pdiInvalidas
                            + " créditos con PDI fuera "
                            + "del rango permitido 0 - 100."
            );
        }


        // =====================================================
        // 5. CALCULAR PÉRDIDA ESPERADA
        //
        // PE =
        //
        // ROUND(
        //      (PI / 100)
        //      * VEA
        //      * (PDI / 100),
        //      0
        // )
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables

                SET perdida_valor =
                    ROUND(
                          (pi / 100.0)
                        * vea
                        * (pdi / 100.0),
                        0
                    )
                """
        );


        // =====================================================
        // 6. PORCENTAJE DE PÉRDIDA
        //
        // Se conserva como proporción decimal.
        //
        // Ejemplo:
        //
        // 0.06219 = 6.219 %
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables

                SET perdida_porce =
                    CASE
                        WHEN vea <> 0
                            THEN perdida_valor / vea

                        ELSE 0
                    END
                """
        );


        // =====================================================
        // 7. CONGELAR BASES PARA DISTRIBUIR DETERIORO
        //
        // capital:
        //     saldo_actual
        //
        // intereses:
        //     saldo_intereses_causados
        //
        // otros:
        //     valor_costas_judiciales
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables

                SET
                    base_deterioro_capital =
                        COALESCE(
                            saldo_actual,
                            0
                        ),

                    base_deterioro_intereses =
                        COALESCE(
                            saldo_intereses_causados,
                            0
                        ),

                    base_deterioro_otros =
                        COALESCE(
                            valor_costas_judiciales,
                            0
                        ),

                    base_deterioro_total =
                          COALESCE(
                              saldo_actual,
                              0
                          )
                        + COALESCE(
                              saldo_intereses_causados,
                              0
                          )
                        + COALESCE(
                              valor_costas_judiciales,
                              0
                          )
                """
        );


        // =====================================================
        // 8. CALCULAR PORCENTAJES DE DISTRIBUCIÓN
        //
        // Se conservan con dos decimales para equivalencia
        // con el proceso histórico.
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables

                SET
                    porcentaje_capital =
                        CASE
                            WHEN base_deterioro_total <> 0
                                THEN ROUND(
                                    (
                                        base_deterioro_capital
                                        * 100.0
                                    )
                                    /
                                    base_deterioro_total,
                                    2
                                )

                            ELSE 0
                        END,

                    porcentaje_intereses =
                        CASE
                            WHEN base_deterioro_total <> 0
                                THEN ROUND(
                                    (
                                        base_deterioro_intereses
                                        * 100.0
                                    )
                                    /
                                    base_deterioro_total,
                                    2
                                )

                            ELSE 0
                        END,

                    porcentaje_otros =
                        CASE
                            WHEN base_deterioro_total <> 0
                                THEN ROUND(
                                    (
                                        base_deterioro_otros
                                        * 100.0
                                    )
                                    /
                                    base_deterioro_total,
                                    2
                                )

                            ELSE 0
                        END
                """
        );


        // =====================================================
        // 9. GUARDAR PORCENTAJES DE DETERIORO
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables

                SET
                    porcentaje_deterioro_capital =
                        porcentaje_capital,

                    porcentaje_deterioro_intereses =
                        porcentaje_intereses,

                    porcentaje_deterioro_otros =
                        porcentaje_otros
                """
        );


        // =====================================================
        // 10. DISTRIBUIR PÉRDIDA - CAPITAL
        //
        // Se utiliza el porcentaje redondeado del proceso.
        //
        // LEAST evita que el componente capital supere la
        // pérdida esperada.
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables

                SET valor_perdida_capital =
                    LEAST(
                        perdida_valor,

                        ROUND(
                            perdida_valor
                            * (
                                porcentaje_deterioro_capital
                                / 100.0
                              ),
                            0
                        )
                    )
                """
        );


        // =====================================================
        // 11. DISTRIBUIR PÉRDIDA - INTERESES
        //
        // Nunca puede superar el remanente después de capital.
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables

                SET valor_perdida_intereses =
                    LEAST(
                        GREATEST(
                            0,
                            perdida_valor
                            - valor_perdida_capital
                        ),

                        ROUND(
                            perdida_valor
                            * (
                                porcentaje_deterioro_intereses
                                / 100.0
                              ),
                            0
                        )
                    )
                """
        );


        // =====================================================
        // 12. DISTRIBUIR PÉRDIDA - OTROS
        //
        // OTROS ES EL REMANENTE.
        //
        // Esto garantiza:
        //
        // capital
        // + intereses
        // + otros
        // = pérdida esperada
        //
        // También reproduce casos históricos como:
        //
        // pérdida        10.566.401
        // capital         9.531.950
        // intereses         254.650
        // otros             779.801
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables

                SET valor_perdida_otros =
                    GREATEST(
                        0,

                        perdida_valor
                        - valor_perdida_capital
                        - valor_perdida_intereses
                    )
                """
        );


        // =====================================================
        // 13. DETERIOROS PE DEFINITIVOS
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables

                SET
                    deterioro_capital_pe =
                        valor_perdida_capital,

                    deterioro_intereses_pe =
                        valor_perdida_intereses,

                    deterioro_otros_pe =
                        valor_perdida_otros,

                    deterioro_total_pe =
                          valor_perdida_capital
                        + valor_perdida_intereses
                        + valor_perdida_otros
                """
        );


        // =====================================================
        // 14. VALIDAR BASES Y RESULTADOS
        // =====================================================

        Integer incompletos =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)

                        FROM tmp_pe_variables

                        WHERE pdi IS NULL

                           OR perdida_valor IS NULL
                           OR perdida_porce IS NULL

                           OR base_deterioro_capital IS NULL
                           OR base_deterioro_intereses IS NULL
                           OR base_deterioro_otros IS NULL
                           OR base_deterioro_total IS NULL

                           OR porcentaje_deterioro_capital IS NULL
                           OR porcentaje_deterioro_intereses IS NULL
                           OR porcentaje_deterioro_otros IS NULL

                           OR valor_perdida_capital IS NULL
                           OR valor_perdida_intereses IS NULL
                           OR valor_perdida_otros IS NULL

                           OR deterioro_capital_pe IS NULL
                           OR deterioro_intereses_pe IS NULL
                           OR deterioro_otros_pe IS NULL
                           OR deterioro_total_pe IS NULL
                        """,
                        Integer.class
                );

        if (incompletos != null
                && incompletos > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + incompletos
                            + " créditos con cálculo "
                            + "PDI / pérdida esperada / deterioro "
                            + "incompleto."
            );
        }


        // =====================================================
        // 15. VALIDAR CONSISTENCIA DE DISTRIBUCIÓN
        //
        // Debe cumplirse exactamente:
        //
        // deterioro_total_pe = perdida_valor
        //
        // Si falla conservamos una muestra diagnóstica.
        // =====================================================

        Integer inconsistentes =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)

                        FROM tmp_pe_variables

                        WHERE deterioro_total_pe <>
                              perdida_valor
                        """,
                        Integer.class
                );

        if (inconsistentes != null
                && inconsistentes > 0) {

            var muestraInconsistencias =
                    jdbcTemplate.queryForList(
                            """
                            SELECT
                                id_cartera_credito,
                                pagare_cartera,
                                id_modelo_pe,

                                saldo_actual,
                                saldo_intereses_causados,
                                valor_costas_judiciales,

                                vea,
                                pi,
                                pdi,

                                perdida_valor,

                                base_deterioro_capital,
                                base_deterioro_intereses,
                                base_deterioro_otros,
                                base_deterioro_total,

                                porcentaje_deterioro_capital,
                                porcentaje_deterioro_intereses,
                                porcentaje_deterioro_otros,

                                valor_perdida_capital,
                                valor_perdida_intereses,
                                valor_perdida_otros,

                                deterioro_capital_pe,
                                deterioro_intereses_pe,
                                deterioro_otros_pe,
                                deterioro_total_pe,

                                (
                                    deterioro_total_pe
                                    - perdida_valor
                                ) AS diferencia

                            FROM tmp_pe_variables

                            WHERE deterioro_total_pe <>
                                  perdida_valor

                            ORDER BY
                                ABS(
                                    deterioro_total_pe
                                    - perdida_valor
                                ) DESC

                            LIMIT 10
                            """
                    );

            throw new IllegalStateException(
                    "Existen "
                            + inconsistentes
                            + " créditos donde el deterioro total PE "
                            + "no coincide con la pérdida esperada. "
                            + "Muestra: "
                            + muestraInconsistencias
            );
        }


        // =====================================================
        // 16. VALIDAR VALORES NEGATIVOS
        // =====================================================

        Integer negativos =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)

                        FROM tmp_pe_variables

                        WHERE perdida_valor < 0

                           OR base_deterioro_capital < 0
                           OR base_deterioro_intereses < 0
                           OR base_deterioro_otros < 0
                           OR base_deterioro_total < 0

                           OR valor_perdida_capital < 0
                           OR valor_perdida_intereses < 0
                           OR valor_perdida_otros < 0

                           OR deterioro_capital_pe < 0
                           OR deterioro_intereses_pe < 0
                           OR deterioro_otros_pe < 0
                           OR deterioro_total_pe < 0
                        """,
                        Integer.class
                );

        if (negativos != null
                && negativos > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + negativos
                            + " créditos con valores negativos "
                            + "en el cálculo de deterioro PE."
            );
        }


        // =====================================================
        // 17. RESULTADO
        // =====================================================

        Integer cantidad =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM tmp_pe_variables
                        """,
                        Integer.class
                );

        return cantidad != null
                ? cantidad
                : 0;
    }
}