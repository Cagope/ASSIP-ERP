package co.assip.erp.cartera.cierremensual.validacion;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ValidacionCierreRepository {

    private final NamedParameterJdbcTemplate jdbc;


    // =========================================================
    // 1. CANTIDAD DE CRÉDITOS DE LA CABECERA
    // =========================================================

    public int cantidadCreditosCabecera(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT cantidad_creditos
                FROM cartera.cierres_cartera
                WHERE id_cierre_cartera = :idCierreCartera
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 2. CANTIDAD DE CRÉDITOS EN LA FOTOGRAFÍA
    // =========================================================

    public int cantidadCreditosFotografia(
            Integer idCierreCartera
    ) {

        String sql = """
            SELECT COUNT(*)
            FROM cartera.cierres_cartera_creditos
            WHERE id_cierre_cartera = :idCierreCartera
              AND saldo_actual > 0
            """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 3. CANTIDAD TOTAL DE RESULTADOS
    // =========================================================

    public int cantidadResultados(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.cierres_cartera_resultados
                WHERE id_cierre_cartera = :idCierreCartera
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 4. CRÉDITOS SIN RESULTADO
    //
    // Todo crédito de la fotografía debe tener exactamente
    // un resultado.
    // =========================================================

    public int cantidadCreditosSinResultado(
            Integer idCierreCartera
    ) {

        String sql = """
            SELECT COUNT(*)
            FROM cartera.cierres_cartera_creditos c
            WHERE c.id_cierre_cartera = :idCierreCartera
              AND c.saldo_actual > 0
              AND NOT EXISTS (
                  SELECT 1
                  FROM cartera.cierres_cartera_resultados r
                  WHERE r.id_cierre_cartera = c.id_cierre_cartera
                    AND r.id_cierre_cartera_credito =
                        c.id_cierre_cartera_credito
              )
            """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 5. CRÉDITOS CON RESULTADOS DUPLICADOS
    //
    // Un crédito no puede tener dos resultados.
    // =========================================================

    public int cantidadCreditosConResultadoDuplicado(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM (
                    SELECT
                        id_cierre_cartera_credito
                    FROM cartera.cierres_cartera_resultados
                    WHERE id_cierre_cartera = :idCierreCartera
                    GROUP BY id_cierre_cartera_credito
                    HAVING COUNT(*) <> 1
                ) x
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 6. RESULTADOS CON MÉTODO INVÁLIDO
    //
    // Métodos permitidos:
    //
    // A1 = Anexo 1
    // PE = Pérdida Esperada / Anexo 2
    //
    // NULL también es inválido.
    // =========================================================

    public int cantidadMetodosInvalidos(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.cierres_cartera_resultados
                WHERE id_cierre_cartera = :idCierreCartera
                  AND (
                      codigo_metodo_calculo IS NULL
                      OR UPPER(TRIM(codigo_metodo_calculo))
                         NOT IN ('A1', 'PE')
                  )
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 7. CANTIDAD RESULTADOS A1
    // =========================================================

    public int cantidadResultadosA1(
            Integer idCierreCartera
    ) {

        return cantidadPorMetodo(
                idCierreCartera,
                "A1"
        );
    }


    // =========================================================
    // 8. CANTIDAD RESULTADOS PE
    // =========================================================

    public int cantidadResultadosPe(
            Integer idCierreCartera
    ) {

        return cantidadPorMetodo(
                idCierreCartera,
                "PE"
        );
    }


    // =========================================================
    // 9. CANTIDAD PE_RESULTADOS
    // =========================================================

    public int cantidadPePersistidos(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.pe_resultados
                WHERE id_cierre_cartera = :idCierreCartera
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 10. RESULTADOS PE SIN TRAZABILIDAD PE
    //
    // Todo crédito marcado PE en resultados debe tener
    // exactamente su registro en pe_resultados.
    // =========================================================

    public int cantidadPeSinResultadoPe(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.cierres_cartera_resultados r
                WHERE r.id_cierre_cartera = :idCierreCartera
                  AND UPPER(TRIM(r.codigo_metodo_calculo)) = 'PE'
                  AND NOT EXISTS (
                      SELECT 1
                      FROM cartera.pe_resultados p
                      WHERE p.id_cierre_cartera =
                            r.id_cierre_cartera
                        AND p.id_cierre_cartera_credito =
                            r.id_cierre_cartera_credito
                  )
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 11. PE_RESULTADOS DUPLICADOS POR CRÉDITO
    // =========================================================

    public int cantidadPeDuplicados(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM (
                    SELECT
                        id_cierre_cartera_credito
                    FROM cartera.pe_resultados
                    WHERE id_cierre_cartera = :idCierreCartera
                    GROUP BY id_cierre_cartera_credito
                    HAVING COUNT(*) <> 1
                ) x
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 12. PE HUÉRFANOS
    //
    // No debe existir un pe_resultados para un crédito cuyo
    // método final no sea PE.
    // =========================================================

    public int cantidadPeHuerfanos(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.pe_resultados p
                WHERE p.id_cierre_cartera = :idCierreCartera
                  AND NOT EXISTS (
                      SELECT 1
                      FROM cartera.cierres_cartera_resultados r
                      WHERE r.id_cierre_cartera =
                            p.id_cierre_cartera
                        AND r.id_cierre_cartera_credito =
                            p.id_cierre_cartera_credito
                        AND UPPER(TRIM(r.codigo_metodo_calculo)) = 'PE'
                  )
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 13. EDADES NULAS EN RESULTADOS
    //
    // Estas seis edades son obligatorias al terminar el
    // procesamiento.
    // =========================================================

    public int cantidadEdadesNulas(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.cierres_cartera_resultados
                WHERE id_cierre_cartera = :idCierreCartera
                  AND (
                      edad_riesgo_inicial IS NULL
                      OR edad_de_mora IS NULL
                      OR edad_de_riesgo IS NULL
                      OR edad_de_pe IS NULL
                      OR edad_de_homologacion IS NULL
                      OR edad_contable IS NULL
                  )
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 14. EDADES INVÁLIDAS EN RESULTADOS
    //
    // Valores permitidos:
    // A, B, C, D, E
    // =========================================================

    public int cantidadEdadesInvalidas(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.cierres_cartera_resultados
                WHERE id_cierre_cartera = :idCierreCartera
                  AND (
                      UPPER(TRIM(edad_riesgo_inicial))
                          NOT IN ('A','B','C','D','E')
                      OR UPPER(TRIM(edad_de_mora))
                          NOT IN ('A','B','C','D','E')
                      OR UPPER(TRIM(edad_de_riesgo))
                          NOT IN ('A','B','C','D','E')
                      OR UPPER(TRIM(edad_de_pe))
                          NOT IN ('A','B','C','D','E')
                      OR UPPER(TRIM(edad_de_homologacion))
                          NOT IN ('A','B','C','D','E')
                      OR UPPER(TRIM(edad_contable))
                          NOT IN ('A','B','C','D','E')
                  )
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 15. VALORES MONETARIOS NEGATIVOS EN RESULTADOS
    //
    // Son saldos, bases o deterioros que al finalizar el
    // cálculo no deben quedar negativos.
    // =========================================================

    public int cantidadValoresNegativos(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.cierres_cartera_resultados
                WHERE id_cierre_cartera = :idCierreCartera
                  AND (
                      saldo_actual < 0
                      OR saldo_total_creditos_asociado < 0
                      OR saldo_aportes_fecha_corte < 0
                      OR porcentaje_aportes_credito < 0
                      OR valor_aportes_credito < 0
                      OR cantidad_bienes_garantia < 0
                      OR valor_garantias_total < 0
                      OR porcentaje_garantias_credito < 0
                      OR valor_garantias_credito < 0
                      OR vea < 0
                      OR saldo_intereses_causados < 0
                      OR valor_costas_judiciales < 0
                      OR saldo_seguros < 0
                      OR saldo_alivios < 0
                      OR valor_fondos_garantias < 0
                      OR valor_otros_conceptos < 0
                      OR valor_intereses_causados_mes < 0
                      OR saldo_intereses_contingentes < 0
                      OR valor_intereses_contingentes_mes < 0
                      OR valor_seguros_mes < 0
                      OR valor_alivios_mes < 0
                      OR deterioro_capital < 0
                      OR deterioro_intereses < 0
                      OR deterioro_otros < 0
                      OR pi < 0
                      OR pdi < 0
                      OR perdida_esperada < 0
                  )
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 16. PI / PDI FUERA DE RANGO
    //
    // Ambos porcentajes deben estar entre 0 y 100.
    // =========================================================

    public int cantidadPiPdiFueraRango(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.cierres_cartera_resultados
                WHERE id_cierre_cartera = :idCierreCartera
                  AND (
                      pi < 0
                      OR pi > 100
                      OR pdi < 0
                      OR pdi > 100
                  )
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 17. INCONSISTENCIA PE:
    //
    // deterioro_total_pe debe ser igual a perdida_esperada.
    //
    // Se permite diferencia máxima de $1 por redondeo.
    // =========================================================

    public int cantidadPeDescuadrados(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.pe_resultados
                WHERE id_cierre_cartera = :idCierreCartera
                  AND ABS(
                      deterioro_total_pe
                      - perdida_esperada
                  ) > 1
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 18. VALORES NEGATIVOS EN PE_RESULTADOS
    // =========================================================

    public int cantidadValoresPeNegativos(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.pe_resultados
                WHERE id_cierre_cartera = :idCierreCartera
                  AND (
                      saldo_actual < 0
                      OR saldo_intereses_causados < 0
                      OR saldo_aportes_fecha_corte < 0
                      OR valor_aportes_credito < 0
                      OR valor_costas_judiciales < 0
                      OR valor_otros_conceptos < 0

                      OR pi < 0

                      OR base_vea_capital < 0
                      OR base_vea_intereses < 0
                      OR base_vea_costas_judiciales < 0
                      OR base_vea_otros < 0
                      OR base_vea_aportes < 0
                      OR base_vea_ahorro_permanente < 0
                      OR vea_bruto < 0
                      OR vea_deducciones < 0
                      OR vea < 0

                      OR valor_garantia < 0
                      OR porcentaje_garantia_reconocido < 0
                      OR valor_garantia_reconocido < 0

                      OR pdi < 0
                      OR perdida_esperada < 0
                      OR porcentaje_perdida < 0

                      OR base_deterioro_capital < 0
                      OR base_deterioro_intereses < 0
                      OR base_deterioro_otros < 0
                      OR base_deterioro_total < 0

                      OR porcentaje_deterioro_capital < 0
                      OR porcentaje_deterioro_intereses < 0
                      OR porcentaje_deterioro_otros < 0

                      OR valor_perdida_capital < 0
                      OR valor_perdida_intereses < 0
                      OR valor_perdida_otros < 0

                      OR deterioro_capital_pe < 0
                      OR deterioro_intereses_pe < 0
                      OR deterioro_otros_pe < 0
                      OR deterioro_total_pe < 0
                  )
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 19. PI / PDI FUERA DE RANGO EN PE
    // =========================================================

    public int cantidadPiPdiPeFueraRango(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.pe_resultados
                WHERE id_cierre_cartera = :idCierreCartera
                  AND (
                      pi < 0
                      OR pi > 100
                      OR pdi < 0
                      OR pdi > 100
                  )
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 20. EDADES PE NULAS
    //
    // Validamos las edades finales relevantes del modelo.
    // =========================================================

    public int cantidadEdadesPeNulas(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.pe_resultados
                WHERE id_cierre_cartera = :idCierreCartera
                  AND (
                      edad_mora_entrada_pe IS NULL
                      OR edad_riesgo_entrada_pe IS NULL
                      OR calificacion_modelo IS NULL
                      OR calificacion_pe IS NULL
                      OR edad_deterioro IS NULL
                      OR calificacion_base_pi IS NULL
                      OR edad_homologada_individual IS NULL
                      OR edad_contable_pe IS NULL
                  )
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 21. EDADES / CALIFICACIONES PE INVÁLIDAS
    // =========================================================

    public int cantidadEdadesPeInvalidas(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.pe_resultados
                WHERE id_cierre_cartera = :idCierreCartera
                  AND (
                      UPPER(TRIM(edad_mora_entrada_pe))
                          NOT IN ('A','B','C','D','E')
                      OR UPPER(TRIM(edad_riesgo_entrada_pe))
                          NOT IN ('A','B','C','D','E')
                      OR UPPER(TRIM(calificacion_modelo))
                          NOT IN ('A','B','C','D','E')
                      OR UPPER(TRIM(calificacion_pe))
                          NOT IN ('A','B','C','D','E')
                      OR UPPER(TRIM(edad_deterioro))
                          NOT IN ('A','B','C','D','E')
                      OR UPPER(TRIM(calificacion_base_pi))
                          NOT IN ('A','B','C','D','E')
                      OR UPPER(TRIM(edad_homologada_individual))
                          NOT IN ('A','B','C','D','E')
                      OR UPPER(TRIM(edad_contable_pe))
                          NOT IN ('A','B','C','D','E')
                  )
                """;

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        parametros(idCierreCartera),
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
    }


    // =========================================================
    // 22. PROCESO PE FINALIZADO
    //
    // Debe existir un proceso PE FINALIZADO para el cierre.
    // =========================================================

    public boolean procesoPeFinalizado(
            Integer idCierreCartera
    ) {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM cartera.pe_procesos
                    WHERE id_cierre_cartera = :idCierreCartera
                      AND UPPER(TRIM(estado)) = 'FINALIZADO'
                      AND fecha_fin IS NOT NULL
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
    // MÉTODO AUXILIAR:
    // CANTIDAD POR MÉTODO DE CÁLCULO
    // =========================================================

    private int cantidadPorMetodo(
            Integer idCierreCartera,
            String metodo
    ) {

        String sql = """
                SELECT COUNT(*)
                FROM cartera.cierres_cartera_resultados
                WHERE id_cierre_cartera = :idCierreCartera
                  AND UPPER(TRIM(codigo_metodo_calculo)) = :metodo
                """;

        MapSqlParameterSource params =
                parametros(idCierreCartera)
                        .addValue(
                                "metodo",
                                metodo
                        );

        Integer resultado =
                jdbc.queryForObject(
                        sql,
                        params,
                        Integer.class
                );

        return resultado == null ? 0 : resultado;
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
}