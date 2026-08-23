package co.assip.erp.cartera.cierremensual.cuadre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
@RequiredArgsConstructor
public class CuadreCierreRepository {

    private final NamedParameterJdbcTemplate jdbc;


    // =========================================================
    // VALIDAR EXISTENCIA DEL CIERRE
    // =========================================================

    public boolean existeCierre(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM cartera.cierres_cartera
                    WHERE id_cierre_cartera = :idCierreCartera
                )
                """;

        Boolean resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Boolean.class
                );

        return Boolean.TRUE.equals(resultado);
    }


    // =========================================================
    // CONSOLIDADO GENERAL
    //
    // Fuente:
    //
    // cartera.cierres_cartera_resultados
    //
    // Se obtiene en una sola consulta:
    //
    // - TOTAL
    // - A1
    // - PE
    //
    // IMPORTANTE:
    //
    // Este método NO modifica información.
    //
    // =========================================================

    public ConsolidadoResultados obtenerConsolidadoResultados(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT

                    -- ==========================================
                    -- CANTIDADES
                    -- ==========================================

                    COUNT(*)::integer
                        AS cantidad_creditos_total,

                    COUNT(*) FILTER (
                        WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                    )::integer
                        AS cantidad_creditos_a1,

                    COUNT(*) FILTER (
                        WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                    )::integer
                        AS cantidad_creditos_pe,


                    -- ==========================================
                    -- SALDO CAPITAL
                    -- ==========================================

                    COALESCE(
                        SUM(saldo_actual),
                        0
                    ) AS saldo_actual_total,

                    COALESCE(
                        SUM(saldo_actual) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS saldo_actual_a1,

                    COALESCE(
                        SUM(saldo_actual) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS saldo_actual_pe,


                    -- ==========================================
                    -- INTERESES CAUSADOS
                    -- ==========================================

                    COALESCE(
                        SUM(saldo_intereses_causados),
                        0
                    ) AS saldo_intereses_causados_total,

                    COALESCE(
                        SUM(saldo_intereses_causados) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS saldo_intereses_causados_a1,

                    COALESCE(
                        SUM(saldo_intereses_causados) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS saldo_intereses_causados_pe,


                    COALESCE(
                        SUM(valor_intereses_causados_mes),
                        0
                    ) AS valor_intereses_causados_mes_total,

                    COALESCE(
                        SUM(valor_intereses_causados_mes) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS valor_intereses_causados_mes_a1,

                    COALESCE(
                        SUM(valor_intereses_causados_mes) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS valor_intereses_causados_mes_pe,


                    -- ==========================================
                    -- INTERESES CONTINGENTES
                    -- ==========================================

                    COALESCE(
                        SUM(saldo_intereses_contingentes),
                        0
                    ) AS saldo_intereses_contingentes_total,

                    COALESCE(
                        SUM(saldo_intereses_contingentes) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS saldo_intereses_contingentes_a1,

                    COALESCE(
                        SUM(saldo_intereses_contingentes) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS saldo_intereses_contingentes_pe,


                    COALESCE(
                        SUM(valor_intereses_contingentes_mes),
                        0
                    ) AS valor_intereses_contingentes_mes_total,

                    COALESCE(
                        SUM(valor_intereses_contingentes_mes) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS valor_intereses_contingentes_mes_a1,

                    COALESCE(
                        SUM(valor_intereses_contingentes_mes) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS valor_intereses_contingentes_mes_pe,


                    -- ==========================================
                    -- SEGUROS
                    -- ==========================================

                    COALESCE(
                        SUM(saldo_seguros),
                        0
                    ) AS saldo_seguros_total,

                    COALESCE(
                        SUM(saldo_seguros) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS saldo_seguros_a1,

                    COALESCE(
                        SUM(saldo_seguros) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS saldo_seguros_pe,


                    COALESCE(
                        SUM(valor_seguros_mes),
                        0
                    ) AS valor_seguros_mes_total,

                    COALESCE(
                        SUM(valor_seguros_mes) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS valor_seguros_mes_a1,

                    COALESCE(
                        SUM(valor_seguros_mes) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS valor_seguros_mes_pe,


                    -- ==========================================
                    -- ALIVIOS
                    -- ==========================================

                    COALESCE(
                        SUM(saldo_alivios),
                        0
                    ) AS saldo_alivios_total,

                    COALESCE(
                        SUM(saldo_alivios) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS saldo_alivios_a1,

                    COALESCE(
                        SUM(saldo_alivios) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS saldo_alivios_pe,


                    COALESCE(
                        SUM(valor_alivios_mes),
                        0
                    ) AS valor_alivios_mes_total,

                    COALESCE(
                        SUM(valor_alivios_mes) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS valor_alivios_mes_a1,

                    COALESCE(
                        SUM(valor_alivios_mes) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS valor_alivios_mes_pe,


                    -- ==========================================
                    -- COSTAS JUDICIALES
                    -- ==========================================

                    COALESCE(
                        SUM(valor_costas_judiciales),
                        0
                    ) AS valor_costas_judiciales_total,

                    COALESCE(
                        SUM(valor_costas_judiciales) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS valor_costas_judiciales_a1,

                    COALESCE(
                        SUM(valor_costas_judiciales) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS valor_costas_judiciales_pe,


                    -- ==========================================
                    -- APORTES
                    -- ==========================================

                    COALESCE(
                        SUM(saldo_aportes_fecha_corte),
                        0
                    ) AS saldo_aportes_fecha_corte_total,

                    COALESCE(
                        SUM(saldo_aportes_fecha_corte) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS saldo_aportes_fecha_corte_a1,

                    COALESCE(
                        SUM(saldo_aportes_fecha_corte) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS saldo_aportes_fecha_corte_pe,


                    COALESCE(
                        SUM(valor_aportes_credito),
                        0
                    ) AS valor_aportes_credito_total,

                    COALESCE(
                        SUM(valor_aportes_credito) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS valor_aportes_credito_a1,

                    COALESCE(
                        SUM(valor_aportes_credito) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS valor_aportes_credito_pe,


                    -- ==========================================
                    -- GARANTÍAS
                    -- ==========================================

                    COALESCE(
                        SUM(valor_garantias_total),
                        0
                    ) AS valor_garantias_total,

                    COALESCE(
                        SUM(valor_garantias_total) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS valor_garantias_a1,

                    COALESCE(
                        SUM(valor_garantias_total) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS valor_garantias_pe,


                    COALESCE(
                        SUM(valor_garantias_credito),
                        0
                    ) AS valor_garantias_credito_total,

                    COALESCE(
                        SUM(valor_garantias_credito) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS valor_garantias_credito_a1,

                    COALESCE(
                        SUM(valor_garantias_credito) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS valor_garantias_credito_pe,


                    -- ==========================================
                    -- FONDOS DE GARANTÍAS
                    -- ==========================================

                    COALESCE(
                        SUM(valor_fondos_garantias),
                        0
                    ) AS valor_fondos_garantias_total,

                    COALESCE(
                        SUM(valor_fondos_garantias) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS valor_fondos_garantias_a1,

                    COALESCE(
                        SUM(valor_fondos_garantias) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS valor_fondos_garantias_pe,


                    -- ==========================================
                    -- OTROS CONCEPTOS
                    -- ==========================================

                    COALESCE(
                        SUM(valor_otros_conceptos),
                        0
                    ) AS valor_otros_conceptos_total,

                    COALESCE(
                        SUM(valor_otros_conceptos) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS valor_otros_conceptos_a1,

                    COALESCE(
                        SUM(valor_otros_conceptos) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS valor_otros_conceptos_pe,


                    -- ==========================================
                    -- DETERIORO CAPITAL
                    -- ==========================================

                    COALESCE(
                        SUM(deterioro_capital),
                        0
                    ) AS deterioro_capital_total,

                    COALESCE(
                        SUM(deterioro_capital) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS deterioro_capital_a1,

                    COALESCE(
                        SUM(deterioro_capital) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS deterioro_capital_pe,


                    -- ==========================================
                    -- DETERIORO INTERESES
                    -- ==========================================

                    COALESCE(
                        SUM(deterioro_intereses),
                        0
                    ) AS deterioro_intereses_total,

                    COALESCE(
                        SUM(deterioro_intereses) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS deterioro_intereses_a1,

                    COALESCE(
                        SUM(deterioro_intereses) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS deterioro_intereses_pe,


                    -- ==========================================
                    -- DETERIORO OTROS
                    -- ==========================================

                    COALESCE(
                        SUM(deterioro_otros),
                        0
                    ) AS deterioro_otros_total,

                    COALESCE(
                        SUM(deterioro_otros) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS deterioro_otros_a1,

                    COALESCE(
                        SUM(deterioro_otros) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS deterioro_otros_pe,


                    -- ==========================================
                    -- PÉRDIDA ESPERADA
                    -- ==========================================

                    COALESCE(
                        SUM(perdida_esperada),
                        0
                    ) AS perdida_esperada_total,

                    COALESCE(
                        SUM(perdida_esperada) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'A1'
                        ),
                        0
                    ) AS perdida_esperada_a1,

                    COALESCE(
                        SUM(perdida_esperada) FILTER (
                            WHERE UPPER(TRIM(codigo_metodo_calculo)) = 'PE'
                        ),
                        0
                    ) AS perdida_esperada_pe

                FROM cartera.cierres_cartera_resultados

                WHERE id_cierre_cartera =
                      :idCierreCartera
                """;

        return jdbc.queryForObject(
                sql,
                parametros(idCierreCartera),
                (rs, rowNum) ->
                        new ConsolidadoResultados(

                                rs.getInt(
                                        "cantidad_creditos_total"
                                ),

                                rs.getInt(
                                        "cantidad_creditos_a1"
                                ),

                                rs.getInt(
                                        "cantidad_creditos_pe"
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_actual_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_actual_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_actual_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_intereses_causados_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_intereses_causados_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_intereses_causados_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "valor_intereses_causados_mes_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_intereses_causados_mes_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_intereses_causados_mes_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_intereses_contingentes_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_intereses_contingentes_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_intereses_contingentes_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "valor_intereses_contingentes_mes_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_intereses_contingentes_mes_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_intereses_contingentes_mes_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_seguros_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_seguros_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_seguros_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "valor_seguros_mes_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_seguros_mes_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_seguros_mes_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_alivios_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_alivios_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_alivios_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "valor_alivios_mes_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_alivios_mes_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_alivios_mes_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "valor_costas_judiciales_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_costas_judiciales_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_costas_judiciales_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_aportes_fecha_corte_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_aportes_fecha_corte_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "saldo_aportes_fecha_corte_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "valor_aportes_credito_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_aportes_credito_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_aportes_credito_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "valor_garantias_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_garantias_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_garantias_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "valor_garantias_credito_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_garantias_credito_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_garantias_credito_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "valor_fondos_garantias_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_fondos_garantias_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_fondos_garantias_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "valor_otros_conceptos_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_otros_conceptos_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "valor_otros_conceptos_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "deterioro_capital_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "deterioro_capital_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "deterioro_capital_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "deterioro_intereses_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "deterioro_intereses_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "deterioro_intereses_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "deterioro_otros_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "deterioro_otros_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "deterioro_otros_pe"
                                        )
                                ),


                                decimal(
                                        rs.getBigDecimal(
                                                "perdida_esperada_total"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "perdida_esperada_a1"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "perdida_esperada_pe"
                                        )
                                )
                        )
        );
    }


    // =========================================================
    // CONSOLIDADO DETALLE PE
    //
    // Fuente:
    //
    // cartera.pe_resultados
    //
    // Este consolidado es independiente del resultado general
    // y se utiliza para comprobar que lo persistido en PE
    // coincide exactamente con:
    //
    // cartera.cierres_cartera_resultados
    //
    // para la población cuyo método final es PE.
    //
    // =========================================================

    public ConsolidadoPeDetalle obtenerConsolidadoPeDetalle(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT

                    COUNT(*)::integer
                        AS cantidad_pe,

                    COALESCE(
                        SUM(perdida_esperada),
                        0
                    ) AS perdida_esperada,

                    COALESCE(
                        SUM(deterioro_capital_pe),
                        0
                    ) AS deterioro_capital_pe,

                    COALESCE(
                        SUM(deterioro_intereses_pe),
                        0
                    ) AS deterioro_intereses_pe,

                    COALESCE(
                        SUM(deterioro_otros_pe),
                        0
                    ) AS deterioro_otros_pe,

                    COALESCE(
                        SUM(deterioro_total_pe),
                        0
                    ) AS deterioro_total_pe

                FROM cartera.pe_resultados

                WHERE id_cierre_cartera =
                      :idCierreCartera
                """;

        return jdbc.queryForObject(
                sql,
                parametros(idCierreCartera),
                (rs, rowNum) ->
                        new ConsolidadoPeDetalle(

                                rs.getInt(
                                        "cantidad_pe"
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "perdida_esperada"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "deterioro_capital_pe"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "deterioro_intereses_pe"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "deterioro_otros_pe"
                                        )
                                ),

                                decimal(
                                        rs.getBigDecimal(
                                                "deterioro_total_pe"
                                        )
                                )
                        )
        );
    }


    // =========================================================
    // PARÁMETROS COMUNES
    // =========================================================

    private MapSqlParameterSource parametros(
            Integer idCierreCartera
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        "idCierreCartera",
                        idCierreCartera
                );
    }


    // =========================================================
    // NORMALIZAR BIGDECIMAL
    // =========================================================

    private BigDecimal decimal(
            BigDecimal valor
    ) {

        return valor != null
                ? valor
                : BigDecimal.ZERO;
    }


    // =========================================================
    // CONSOLIDADO DE RESULTADOS
    // =========================================================

    public record ConsolidadoResultados(

            Integer cantidadCreditosTotal,
            Integer cantidadCreditosA1,
            Integer cantidadCreditosPe,

            BigDecimal saldoActualTotal,
            BigDecimal saldoActualA1,
            BigDecimal saldoActualPe,

            BigDecimal saldoInteresesCausadosTotal,
            BigDecimal saldoInteresesCausadosA1,
            BigDecimal saldoInteresesCausadosPe,

            BigDecimal valorInteresesCausadosMesTotal,
            BigDecimal valorInteresesCausadosMesA1,
            BigDecimal valorInteresesCausadosMesPe,

            BigDecimal saldoInteresesContingentesTotal,
            BigDecimal saldoInteresesContingentesA1,
            BigDecimal saldoInteresesContingentesPe,

            BigDecimal valorInteresesContingentesMesTotal,
            BigDecimal valorInteresesContingentesMesA1,
            BigDecimal valorInteresesContingentesMesPe,

            BigDecimal saldoSegurosTotal,
            BigDecimal saldoSegurosA1,
            BigDecimal saldoSegurosPe,

            BigDecimal valorSegurosMesTotal,
            BigDecimal valorSegurosMesA1,
            BigDecimal valorSegurosMesPe,

            BigDecimal saldoAliviosTotal,
            BigDecimal saldoAliviosA1,
            BigDecimal saldoAliviosPe,

            BigDecimal valorAliviosMesTotal,
            BigDecimal valorAliviosMesA1,
            BigDecimal valorAliviosMesPe,

            BigDecimal valorCostasJudicialesTotal,
            BigDecimal valorCostasJudicialesA1,
            BigDecimal valorCostasJudicialesPe,

            BigDecimal saldoAportesFechaCorteTotal,
            BigDecimal saldoAportesFechaCorteA1,
            BigDecimal saldoAportesFechaCortePe,

            BigDecimal valorAportesCreditoTotal,
            BigDecimal valorAportesCreditoA1,
            BigDecimal valorAportesCreditoPe,

            BigDecimal valorGarantiasTotal,
            BigDecimal valorGarantiasA1,
            BigDecimal valorGarantiasPe,

            BigDecimal valorGarantiasCreditoTotal,
            BigDecimal valorGarantiasCreditoA1,
            BigDecimal valorGarantiasCreditoPe,

            BigDecimal valorFondosGarantiasTotal,
            BigDecimal valorFondosGarantiasA1,
            BigDecimal valorFondosGarantiasPe,

            BigDecimal valorOtrosConceptosTotal,
            BigDecimal valorOtrosConceptosA1,
            BigDecimal valorOtrosConceptosPe,

            BigDecimal deterioroCapitalTotal,
            BigDecimal deterioroCapitalA1,
            BigDecimal deterioroCapitalPe,

            BigDecimal deterioroInteresesTotal,
            BigDecimal deterioroInteresesA1,
            BigDecimal deterioroInteresesPe,

            BigDecimal deterioroOtrosTotal,
            BigDecimal deterioroOtrosA1,
            BigDecimal deterioroOtrosPe,

            BigDecimal perdidaEsperadaTotal,
            BigDecimal perdidaEsperadaA1,
            BigDecimal perdidaEsperadaPe

    ) {
    }


    // =========================================================
    // CONSOLIDADO PE DETALLE
    // =========================================================

    public record ConsolidadoPeDetalle(

            Integer cantidadPe,

            BigDecimal perdidaEsperada,

            BigDecimal deterioroCapitalPe,

            BigDecimal deterioroInteresesPe,

            BigDecimal deterioroOtrosPe,

            BigDecimal deterioroTotalPe

    ) {
    }
}