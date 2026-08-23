package co.assip.erp.cartera.cierremensual.anexo2;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class Anexo2PoblacionRepository {

    private final JdbcTemplate jdbcTemplate;

    // Población PE, matriz histórica de moras y variables temporales.

    public int crearPoblacionTemporal(
            Integer idCierreCartera
    ) {

        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS tmp_pe_poblacion"
        );

        String sql = """
        CREATE TEMP TABLE tmp_pe_poblacion
        ON COMMIT DROP
        AS

        SELECT
            f.id_cierre_cartera_credito,
            f.id_cartera_credito,
            f.id_datos_personal,
            f.pagare_cartera,
            f.documento,
            f.codigo_clasificacion_credito,
            f.codigo_forma_pago,
            f.id_empresa_libranza,
            f.codigo_garantia_credito,

            f.saldo_actual,
            f.fecha_desembolso,

            r.saldo_intereses_causados,
            r.saldo_aportes_fecha_corte,
            r.valor_aportes_credito,
            r.valor_costas_judiciales,
            r.valor_otros_conceptos,
            r.dias_mora,
            r.edad_de_mora,
            r.edad_de_riesgo,

            hv.tipo_persona,

            CASE

                -- =========================================
                -- 1. CONSUMO CON LIBRANZA
                -- =========================================
                WHEN f.codigo_clasificacion_credito = 'S'
                     AND (
                          f.id_empresa_libranza IS NOT NULL
                          OR f.codigo_forma_pago = 'L'
                     )
                THEN 1

                -- =========================================
                -- 2. CONSUMO SIN LIBRANZA
                -- =========================================
                WHEN f.codigo_clasificacion_credito = 'S'
                THEN 2

                -- =========================================
                -- 3. COMERCIAL PERSONA NATURAL
                -- =========================================
                WHEN f.codigo_clasificacion_credito = 'C'
                     AND hv.tipo_persona = '1'
                THEN 3

                ELSE NULL

            END AS id_modelo_pe

        FROM cartera.cierres_cartera_creditos f

        INNER JOIN cartera.cierres_cartera c
            ON c.id_cierre_cartera =
               f.id_cierre_cartera

        INNER JOIN cartera.cierres_cartera_resultados r
            ON r.id_cierre_cartera_credito =
               f.id_cierre_cartera_credito

        LEFT JOIN hoja_vida.cierres_hoja_vida chv
            ON chv.fecha_corte =
               c.fecha_corte

        LEFT JOIN hoja_vida.cierres_hoja_vida_personas hv
            ON hv.id_cierre_hoja_vida =
               chv.id_cierre_hoja_vida
           AND hv.id_datos_personal =
               f.id_datos_personal

        WHERE f.id_cierre_cartera = ?
          AND COALESCE(f.saldo_actual, 0) > 0

          AND (
                f.codigo_clasificacion_credito = 'S'

                OR (
                    f.codigo_clasificacion_credito = 'C'
                    AND hv.tipo_persona = '1'
                )
              )
        """;

        jdbcTemplate.update(
                sql,
                idCierreCartera
        );

        Integer cantidad = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tmp_pe_poblacion
                WHERE id_modelo_pe IS NOT NULL
                """,
                Integer.class
        );

        return cantidad != null ? cantidad : 0;
    }


    public long crearMorasTemporales(
            Integer idCierreCartera
    ) {

        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS tmp_pe_moras"
        );

        String sql = """
            CREATE TEMP TABLE tmp_pe_moras
            ON COMMIT DROP
            AS

            WITH parametros AS
            (
                SELECT
                    id_cierre_cartera,
                    fecha_corte
                FROM cartera.cierres_cartera
                WHERE id_cierre_cartera = ?
            ),

            cierres_40 AS
            (
                SELECT
                    ch.id_cierre_cartera,
                    ch.fecha_corte,

                    ROW_NUMBER() OVER (
                        ORDER BY ch.fecha_corte
                    )::integer AS numero_periodo

                FROM cartera.cierres_cartera ch

                CROSS JOIN parametros p

                WHERE ch.fecha_corte BETWEEN
                      (p.fecha_corte - INTERVAL '39 months')::date
                      AND p.fecha_corte
            )

            SELECT
                p.id_cierre_cartera_credito,
                p.id_cartera_credito,
                p.id_modelo_pe,
                p.pagare_cartera,

                c.numero_periodo,

                c.id_cierre_cartera
                    AS id_cierre_cartera_historico,

                c.fecha_corte
                    AS fecha_corte_historico,

                COALESCE(rh.dias_mora, 0)::integer
                    AS dias_mora

            FROM tmp_pe_poblacion p

            CROSS JOIN cierres_40 c

            LEFT JOIN cartera.cierres_cartera_creditos fh
                ON fh.id_cierre_cartera =
                   c.id_cierre_cartera
               AND fh.id_cartera_credito =
                   p.id_cartera_credito

            LEFT JOIN cartera.cierres_cartera_resultados rh
                ON rh.id_cierre_cartera_credito =
                   fh.id_cierre_cartera_credito

            WHERE p.id_modelo_pe IS NOT NULL
            """;

        jdbcTemplate.update(
                sql,
                idCierreCartera
        );

        Long cantidad = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tmp_pe_moras
                """,
                Long.class
        );

        return cantidad != null ? cantidad : 0L;
    }


    public Integer cantidadPeriodosMora() {

        Integer cantidad = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(DISTINCT numero_periodo)
                FROM tmp_pe_moras
                """,
                Integer.class
        );

        return cantidad != null ? cantidad : 0;
    }


    public Integer maximoPeriodoMora() {

        Integer periodo = jdbcTemplate.queryForObject(
                """
                SELECT MAX(numero_periodo)
                FROM tmp_pe_moras
                """,
                Integer.class
        );

        return periodo != null ? periodo : 0;
    }


    public int cantidadCortesHistoricos() {

        Integer cantidad = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(
                    DISTINCT id_cierre_cartera_historico
                )
                FROM tmp_pe_moras
                """,
                Integer.class
        );

        return cantidad != null ? cantidad : 0;
    }


    public boolean validarCorte40(
            Integer idCierreCartera
    ) {

        Boolean valido = jdbcTemplate.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1
                    FROM tmp_pe_moras tm

                    INNER JOIN cartera.cierres_cartera c
                        ON c.id_cierre_cartera = ?

                    WHERE tm.numero_periodo = 40
                      AND tm.fecha_corte_historico =
                          c.fecha_corte
                )
                """,
                Boolean.class,
                idCierreCartera
        );

        return Boolean.TRUE.equals(valido);
    }


    public int crearVariablesTemporales() {

        // =====================================================
        // 1. LIMPIAR TEMPORAL ANTERIOR
        // =====================================================

        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS tmp_pe_variables"
        );

        // =====================================================
        // 2. CREAR TEMPORAL
        // =====================================================

        String sql = """
        CREATE TEMP TABLE tmp_pe_variables
        ON COMMIT DROP
        AS

        WITH resumen_moras AS
        (
            SELECT
                m.id_cierre_cartera_credito,
                m.id_cartera_credito,
                m.id_modelo_pe,
                m.pagare_cartera,

                MAX(m.dias_mora) FILTER (
                    WHERE m.numero_periodo BETWEEN 37 AND 40
                ) AS mora_max_3m,

                MAX(m.dias_mora) FILTER (
                    WHERE m.numero_periodo BETWEEN 28 AND 40
                ) AS mora_max_12m,

                MAX(m.dias_mora) FILTER (
                    WHERE m.numero_periodo BETWEEN 16 AND 40
                ) AS mora_max_24m,

                MAX(m.dias_mora) FILTER (
                    WHERE m.numero_periodo BETWEEN 4 AND 40
                ) AS mora_max_36m,

                COUNT(*) FILTER (
                    WHERE m.numero_periodo BETWEEN 37 AND 40
                      AND m.dias_mora BETWEEN 31 AND 60
                ) AS cantidad_mora_31_60_3m

            FROM tmp_pe_moras m

            GROUP BY
                m.id_cierre_cartera_credito,
                m.id_cartera_credito,
                m.id_modelo_pe,
                m.pagare_cartera
        )

        SELECT
            -- =================================================
            -- 1. IDENTIFICACIÓN
            -- =================================================

            p.id_cierre_cartera_credito,
            p.id_cartera_credito,
            p.id_modelo_pe,

            p.pagare_cartera,
            p.documento,


            -- =================================================
            -- 2. DATOS DE ENTRADA AL PE
            -- =================================================

            p.codigo_clasificacion_credito,
            p.codigo_forma_pago,
            p.id_empresa_libranza,
            p.tipo_persona,
            p.codigo_garantia_credito,

            p.saldo_actual,
            p.saldo_intereses_causados,
            p.saldo_aportes_fecha_corte,
            p.valor_aportes_credito,
            p.valor_costas_judiciales,
            p.valor_otros_conceptos,

            p.fecha_desembolso,

            p.dias_mora
                AS dias_mora_actual,


            -- =================================================
            -- 3. EDADES DE ENTRADA AL PE
            -- =================================================

            CASE
                WHEN p.edad_de_mora IS NULL
                  OR TRIM(p.edad_de_mora) = ''
                    THEN 'A'

                WHEN p.edad_de_mora = 'F'
                    THEN 'E'

                ELSE p.edad_de_mora
            END AS edad_de_mora,

            CASE
                WHEN p.edad_de_riesgo IS NULL
                  OR TRIM(p.edad_de_riesgo) = ''
                    THEN 'A'

                WHEN p.edad_de_riesgo = 'F'
                    THEN 'E'

                ELSE p.edad_de_riesgo
            END AS edad_de_riesgo,


            -- =================================================
            -- 4. RESUMEN HISTÓRICO DE MORAS
            -- =================================================

            COALESCE(
                rm.mora_max_3m,
                0
            ) AS mora_max_3m,

            COALESCE(
                rm.mora_max_12m,
                0
            ) AS mora_max_12m,

            COALESCE(
                rm.mora_max_24m,
                0
            ) AS mora_max_24m,

            COALESCE(
                rm.mora_max_36m,
                0
            ) AS mora_max_36m,

            COALESCE(
                rm.cantidad_mora_31_60_3m,
                0
            ) AS cantidad_mora_31_60_3m,


            -- =================================================
            -- 5. EA
            --
            -- Se conserva por ahora exactamente la regla
            -- actualmente implementada:
            --
            -- saldo_aportes_fecha_corte > 0 -> EA = 1
            --
            -- ea_contenido conserva el valor observado.
            -- =================================================

            CASE
                WHEN COALESCE(
                        p.saldo_aportes_fecha_corte,
                        0
                     ) > 0
                    THEN 1

                ELSE 0
            END AS ea,

            COALESCE(
                p.saldo_aportes_fecha_corte,
                0
            ) AS ea_contenido,


            -- =================================================
            -- 6. VARIABLES CONSUMO CON LIBRANZA
            --
            -- Todavía NO implementadas:
            --
            -- FE
            -- VALCUOTA
            -- FONDPLAZO
            --
            -- Se crean como NULL para que la estructura
            -- temporal y pe_resultados queden preparadas.
            -- =================================================

            CAST(NULL AS INTEGER)
                AS fe,

            CAST(NULL AS VARCHAR(100))
                AS fe_contenido,

            CAST(NULL AS INTEGER)
                AS valcuota,

            CAST(NULL AS NUMERIC(18,2))
                AS valcuota_contenido,

            CAST(NULL AS INTEGER)
                AS fondplazo,

            CAST(NULL AS INTEGER)
                AS fondplazo_contenido,


            -- =================================================
            -- 7. MORA1230
            --
            -- Máxima mora últimos 12 meses:
            -- 31 a 60 días.
            -- =================================================

            CASE
                WHEN rm.mora_max_12m
                     BETWEEN 31 AND 60
                    THEN 1

                ELSE 0
            END AS mora1230,

            COALESCE(
                rm.mora_max_12m,
                0
            ) AS mora1230_contenido,


            -- =================================================
            -- 8. MORA1260
            --
            -- Máxima mora últimos 12 meses > 60.
            -- =================================================

            CASE
                WHEN rm.mora_max_12m > 60
                    THEN 1

                ELSE 0
            END AS mora1260,

            COALESCE(
                rm.mora_max_12m,
                0
            ) AS mora1260_contenido,


            -- =================================================
            -- 9. SINMORA
            --
            -- No presentó mora > 30 en últimos 36 meses.
            -- =================================================

            CASE
                WHEN rm.mora_max_36m <= 30
                    THEN 1

                ELSE 0
            END AS sinmora,

            COALESCE(
                rm.mora_max_36m,
                0
            ) AS sinmora_contenido,


            -- =================================================
            -- 10. MORA2430N
            --
            -- Mora máxima últimos 24 meses entre 31 y 60
            -- y MORA1230 = 0.
            -- =================================================

            CASE
                WHEN rm.mora_max_24m
                     BETWEEN 31 AND 60

                 AND NOT (
                        rm.mora_max_12m
                        BETWEEN 31 AND 60
                     )

                    THEN 1

                ELSE 0
            END AS mora2430n,

            COALESCE(
                rm.mora_max_24m,
                0
            ) AS mora2430n_contenido,


            -- =================================================
            -- 11. MORA315
            --
            -- Modelo 2:
            -- CONSUMO SIN LIBRANZA
            --
            -- Mora máxima últimos 3 meses entre 16 y 30.
            --
            -- NULL cuando no pertenece al modelo.
            -- =================================================

            CASE
                WHEN p.id_modelo_pe = 2
                 AND rm.mora_max_3m
                     BETWEEN 16 AND 30
                    THEN 1

                WHEN p.id_modelo_pe = 2
                    THEN 0

                ELSE NULL
            END AS mora315,

            CASE
                WHEN p.id_modelo_pe = 2
                    THEN COALESCE(
                        rm.mora_max_3m,
                        0
                    )

                ELSE NULL
            END AS mora315_contenido,


            -- =================================================
            -- 12. MORTRIM
            --
            -- Modelo 3:
            -- COMERCIAL PERSONA NATURAL
            --
            -- Una o más moras entre 31 y 60 días
            -- dentro de la ventana reciente.
            --
            -- NULL cuando no pertenece al modelo.
            -- =================================================

            CASE
                WHEN p.id_modelo_pe = 3
                 AND rm.cantidad_mora_31_60_3m >= 1
                    THEN 1

                WHEN p.id_modelo_pe = 3
                    THEN 0

                ELSE NULL
            END AS mortrim,

            CASE
                WHEN p.id_modelo_pe = 3
                    THEN COALESCE(
                        rm.cantidad_mora_31_60_3m,
                        0
                    )

                ELSE NULL
            END AS mortrim_contenido,


            -- =================================================
            -- 13. MORA3660
            --
            -- Modelo 1:
            -- CONSUMO CON LIBRANZA
            --
            -- Mora máxima 36 meses >= 60
            -- y mora máxima 24 meses = 0.
            --
            -- Se conservan los dos valores observados.
            -- =================================================

            CASE
                WHEN p.id_modelo_pe = 1
                 AND rm.mora_max_36m >= 60
                 AND rm.mora_max_24m = 0
                    THEN 1

                WHEN p.id_modelo_pe = 1
                    THEN 0

                ELSE NULL
            END AS mora3660,

            CASE
                WHEN p.id_modelo_pe = 1
                    THEN COALESCE(
                        rm.mora_max_36m,
                        0
                    )

                ELSE NULL
            END AS mora3660_mora_max_36m,

            CASE
                WHEN p.id_modelo_pe = 1
                    THEN COALESCE(
                        rm.mora_max_24m,
                        0
                    )

                ELSE NULL
            END AS mora3660_mora_max_24m,


            -- =================================================
            -- 14. BETAS / COEFICIENTES
            --
            -- Se cargan posteriormente en:
            --
            -- calcularZPuntajeCalificacion()
            -- =================================================

            CAST(NULL AS NUMERIC(18,10))
                AS beta_intercepto,

            CAST(NULL AS NUMERIC(18,10))
                AS beta_ea,

            CAST(NULL AS NUMERIC(18,10))
                AS beta_fe,

            CAST(NULL AS NUMERIC(18,10))
                AS beta_valcuota,

            CAST(NULL AS NUMERIC(18,10))
                AS beta_fondplazo,

            CAST(NULL AS NUMERIC(18,10))
                AS beta_mora1230,

            CAST(NULL AS NUMERIC(18,10))
                AS beta_mora1260,

            CAST(NULL AS NUMERIC(18,10))
                AS beta_sinmora,

            CAST(NULL AS NUMERIC(18,10))
                AS beta_mora2430n,

            CAST(NULL AS NUMERIC(18,10))
                AS beta_mora315,

            CAST(NULL AS NUMERIC(18,10))
                AS beta_mortrim,

            CAST(NULL AS NUMERIC(18,10))
                AS beta_mora3660,


            -- =================================================
            -- 15. APORTE INDIVIDUAL A Z
            -- =================================================

            CAST(NULL AS NUMERIC(18,10))
                AS aporte_z_intercepto,

            CAST(NULL AS NUMERIC(18,10))
                AS aporte_z_ea,

            CAST(NULL AS NUMERIC(18,10))
                AS aporte_z_fe,

            CAST(NULL AS NUMERIC(18,10))
                AS aporte_z_valcuota,

            CAST(NULL AS NUMERIC(18,10))
                AS aporte_z_fondplazo,

            CAST(NULL AS NUMERIC(18,10))
                AS aporte_z_mora1230,

            CAST(NULL AS NUMERIC(18,10))
                AS aporte_z_mora1260,

            CAST(NULL AS NUMERIC(18,10))
                AS aporte_z_sinmora,

            CAST(NULL AS NUMERIC(18,10))
                AS aporte_z_mora2430n,

            CAST(NULL AS NUMERIC(18,10))
                AS aporte_z_mora315,

            CAST(NULL AS NUMERIC(18,10))
                AS aporte_z_mortrim,

            CAST(NULL AS NUMERIC(18,10))
                AS aporte_z_mora3660,


            -- =================================================
            -- 16. RESULTADO ESTADÍSTICO
            -- =================================================

            CAST(NULL AS NUMERIC(18,10))
                AS z,

            CAST(NULL AS NUMERIC(18,10))
                AS puntaje,

            CAST(NULL AS VARCHAR(1))
                AS calificacion_modelo,


            -- =================================================
            -- 17. DEFAULT / CALIFICACIÓN PE / PI
            -- =================================================

            CAST(NULL AS INTEGER)
                AS dias_default_modelo,

            CAST(NULL AS INTEGER)
                AS default_pe,

            CAST(NULL AS VARCHAR(1))
                AS calificacion_pe,

            CAST(NULL AS VARCHAR(1))
                AS edad_deterioro,

            CAST(NULL AS INTEGER)
                AS tipo_entidad_pe,

            CAST(NULL AS VARCHAR(1))
                AS calificacion_base_pi,

            CAST(NULL AS NUMERIC(12,6))
                AS pi,


            -- =================================================
            -- 18. VEA
            -- =================================================

            CAST(NULL AS INTEGER)
                AS tipo_doc_pe,

            CAST(NULL AS NUMERIC(18,2))
                AS base_vea_capital,

            CAST(NULL AS NUMERIC(18,2))
                AS base_vea_intereses,

            CAST(NULL AS NUMERIC(18,2))
                AS base_vea_costas_judiciales,

            CAST(NULL AS NUMERIC(18,2))
                AS base_vea_otros,

            CAST(NULL AS NUMERIC(18,2))
                AS base_vea_aportes,

            CAST(NULL AS NUMERIC(18,2))
                AS base_vea_ahorro_permanente,

            CAST(NULL AS NUMERIC(18,2))
                AS vea_bruto,

            CAST(NULL AS NUMERIC(18,2))
                AS vea_deducciones,

            CAST(NULL AS NUMERIC(18,2))
                AS vea,


            -- =================================================
            -- 19. GARANTÍA UTILIZADA POR PE
            -- =================================================

            CAST(NULL AS VARCHAR(2))
                AS codigo_garantia_pdi,

            CAST(NULL AS VARCHAR(100))
                AS nombre_garantia_pdi,

            CAST(NULL AS NUMERIC(18,2))
                AS valor_garantia,

            CAST(NULL AS NUMERIC(12,6))
                AS porcentaje_garantia_reconocido,

            CAST(NULL AS NUMERIC(18,2))
                AS valor_garantia_reconocido,


            -- =================================================
            -- 20. PDI
            -- =================================================

            CAST(NULL AS INTEGER)
                AS dias_mora_pdi,

            CAST(NULL AS INTEGER)
                AS tramo_pdi,

            CAST(NULL AS INTEGER)
                AS dias_desde_pdi,

            CAST(NULL AS INTEGER)
                AS dias_hasta_pdi,

            CAST(NULL AS NUMERIC(12,6))
                AS pdi,


            -- =================================================
            -- 21. PÉRDIDA ESPERADA
            -- =================================================

            CAST(NULL AS NUMERIC(18,2))
                AS perdida_valor,

            CAST(NULL AS NUMERIC(18,10))
                AS perdida_porce,


            -- =================================================
            -- 22. BASES PARA DISTRIBUIR EL DETERIORO
            -- =================================================

            CAST(NULL AS NUMERIC(18,2))
                AS base_deterioro_capital,

            CAST(NULL AS NUMERIC(18,2))
                AS base_deterioro_intereses,

            CAST(NULL AS NUMERIC(18,2))
                AS base_deterioro_otros,

            CAST(NULL AS NUMERIC(18,2))
                AS base_deterioro_total,


            -- =================================================
            -- 23. PORCENTAJES ORIGINALES DEL MOTOR
            --
            -- Se mantienen porque actualmente los demás
            -- cálculos ya los utilizan.
            -- =================================================

            CAST(NULL AS NUMERIC(12,6))
                AS porcentaje_capital,

            CAST(NULL AS NUMERIC(12,6))
                AS porcentaje_intereses,

            CAST(NULL AS NUMERIC(12,6))
                AS porcentaje_otros,


            -- =================================================
            -- 24. PORCENTAJES EXPLÍCITOS DE DETERIORO
            --
            -- Son los que se persistirán en pe_resultados.
            -- =================================================

            CAST(NULL AS NUMERIC(12,6))
                AS porcentaje_deterioro_capital,

            CAST(NULL AS NUMERIC(12,6))
                AS porcentaje_deterioro_intereses,

            CAST(NULL AS NUMERIC(12,6))
                AS porcentaje_deterioro_otros,


            -- =================================================
            -- 25. PÉRDIDA DISTRIBUIDA
            -- =================================================

            CAST(NULL AS NUMERIC(18,2))
                AS valor_perdida_capital,

            CAST(NULL AS NUMERIC(18,2))
                AS valor_perdida_intereses,

            CAST(NULL AS NUMERIC(18,2))
                AS valor_perdida_otros,


            -- =================================================
            -- 26. DETERIOROS PE DEFINITIVOS
            -- =================================================

            CAST(NULL AS NUMERIC(18,2))
                AS deterioro_capital_pe,

            CAST(NULL AS NUMERIC(18,2))
                AS deterioro_intereses_pe,

            CAST(NULL AS NUMERIC(18,2))
                AS deterioro_otros_pe,

            CAST(NULL AS NUMERIC(18,2))
                AS deterioro_total_pe,


            -- =================================================
            -- 27. HOMOLOGACIÓN / ALINEACIÓN
            -- =================================================

            CAST(NULL AS INTEGER)
                AS dias_mora_homologacion,

            CAST(NULL AS VARCHAR(1))
                AS edad_homologada_individual,

            CAST(NULL AS VARCHAR(1))
                AS edad_contable_pe


        FROM tmp_pe_poblacion p

        INNER JOIN resumen_moras rm
            ON rm.id_cierre_cartera_credito =
               p.id_cierre_cartera_credito
        """;

        jdbcTemplate.execute(
                sql
        );


        // =====================================================
        // 3. VALIDAR CANTIDAD CREADA
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


    public java.util.List<java.util.Map<String, Object>>
    consultarVariablesMuestra() {

        return jdbcTemplate.queryForList("""
        SELECT
            id_modelo_pe,
            pagare_cartera,
            documento,

            ea,

            mora_max_3m,
            mora_max_12m,
            mora_max_24m,
            mora_max_36m,

            mora1230,
            mora1260,
            sinmora,
            mora2430n,

            mora315,
            mortrim,
            mora3660,

            z,
            puntaje,
            calificacion_modelo,
            calificacion_pe,

            edad_de_mora,
            edad_de_riesgo,
            default_pe,
            edad_deterioro,
            pi,

            dias_mora_actual,

            fecha_desembolso,
            valor_aportes_credito,
            valor_costas_judiciales,

            tipo_doc_pe,
            vea,

            pdi,
            perdida_valor,
            perdida_porce,

            porcentaje_capital,
            porcentaje_intereses,
            porcentaje_otros,

            deterioro_capital_pe,
            deterioro_intereses_pe,
            deterioro_otros_pe,

            edad_homologada_individual,
            edad_contable_pe

        FROM tmp_pe_variables

        WHERE pagare_cartera = '194271'

        ORDER BY
            id_modelo_pe,
            pagare_cartera
        """);
    }


}
