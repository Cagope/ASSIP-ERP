package co.assip.erp.cartera.cierremensual.anexo2;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class Anexo2Repository {

    private final JdbcTemplate jdbcTemplate;

    // =========================================================
    // VALIDAR CIERRE
    // =========================================================

    public boolean existeCierre(Integer idCierreCartera) {

        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM cartera.cierres_cartera
                WHERE id_cierre_cartera = ?
            )
            """;

        Boolean existe = jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                idCierreCartera
        );

        return Boolean.TRUE.equals(existe);
    }


    // =========================================================
    // CREAR / ACTUALIZAR PROCESO PE
    // =========================================================

    public Integer iniciarProceso(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
            INSERT INTO cartera.pe_procesos
            (
                id_cierre_cartera,
                estado,
                fecha_inicio,
                fecha_fin,
                fk_seguridad_creacion
            )
            VALUES
            (
                ?,
                'PROCESANDO',
                CURRENT_TIMESTAMP,
                NULL,
                ?
            )
            ON CONFLICT (id_cierre_cartera)
            DO UPDATE SET
                estado = 'PROCESANDO',
                fecha_inicio = CURRENT_TIMESTAMP,
                fecha_fin = NULL,
                fk_seguridad_creacion = EXCLUDED.fk_seguridad_creacion
            RETURNING id_pe_proceso
            """;

        return jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                idCierreCartera,
                idUsuario
        );
    }


    // =========================================================
    // CREAR POBLACIÓN ANEXO 2
    //
    // MODELOS:
    // 1 = CONSUMO CON LIBRANZA
    // 2 = CONSUMO SIN LIBRANZA
    // 3 = COMERCIAL PERSONA NATURAL
    //
    // Solo créditos con saldo_actual > 0.
    // =========================================================

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

    // =========================================================
    // CREAR MATRIZ DE 40 MORAS
    //
    // corte 1  = fecha trabajo - 39 meses
    // corte 40 = fecha trabajo
    //
    // Cuando un crédito no aparece en un cierre:
    // dias_mora = 0
    //
    // Equivalente funcional de SARC_MORA del proceso VB.
    // =========================================================

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


    // =========================================================
    // VALIDACIONES DE MATRIZ DE MORAS
    // =========================================================

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


    // =========================================================
    // CREAR VARIABLES DEL MODELO PE
    //
    // Se genera una fila por crédito.
    //
    // Ventanas:
    //
    // 3 meses  + actual =  4 cortes -> 37..40
    // 12 meses + actual = 13 cortes -> 28..40
    // 24 meses + actual = 25 cortes -> 16..40
    // 36 meses + actual = 37 cortes ->  4..40
    //
    // =========================================================

    public int crearVariablesTemporales() {

        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS tmp_pe_variables"
        );

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
            p.id_cierre_cartera_credito,
            p.id_cartera_credito,
            p.id_modelo_pe,
            p.pagare_cartera,
            p.documento,
            p.tipo_persona,

            p.saldo_actual,
            p.saldo_intereses_causados,
            p.saldo_aportes_fecha_corte,
            p.valor_otros_conceptos,
            p.codigo_garantia_credito,
            p.fecha_desembolso,
            p.valor_aportes_credito,
            p.valor_costas_judiciales,

            p.dias_mora AS dias_mora_actual,

            -- =====================================================
            -- EDADES DE ENTRADA DESDE ANEXO 1
            -- =====================================================

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

            -- =====================================================
            -- EA
            -- =====================================================

            CASE
                WHEN COALESCE(p.saldo_aportes_fecha_corte, 0) > 0
                    THEN 1
                ELSE 0
            END AS ea,

            -- =====================================================
            -- VALORES BASE DE MORA
            -- =====================================================

            COALESCE(rm.mora_max_3m, 0)
                AS mora_max_3m,

            COALESCE(rm.mora_max_12m, 0)
                AS mora_max_12m,

            COALESCE(rm.mora_max_24m, 0)
                AS mora_max_24m,

            COALESCE(rm.mora_max_36m, 0)
                AS mora_max_36m,

            COALESCE(rm.cantidad_mora_31_60_3m, 0)
                AS cantidad_mora_31_60_3m,

            -- =====================================================
            -- MORA1230
            -- =====================================================

            CASE
                WHEN rm.mora_max_12m BETWEEN 31 AND 60
                    THEN 1
                ELSE 0
            END AS mora1230,

            -- =====================================================
            -- MORA1260
            -- =====================================================

            CASE
                WHEN rm.mora_max_12m > 60
                    THEN 1
                ELSE 0
            END AS mora1260,

            -- =====================================================
            -- SINMORA
            -- =====================================================

            CASE
                WHEN rm.mora_max_36m <= 30
                    THEN 1
                ELSE 0
            END AS sinmora,

            -- =====================================================
            -- MORA2430N
            -- =====================================================

            CASE
                WHEN rm.mora_max_24m BETWEEN 31 AND 60
                 AND NOT (
                        rm.mora_max_12m BETWEEN 31 AND 60
                     )
                    THEN 1
                ELSE 0
            END AS mora2430n,

            -- =====================================================
            -- MODELO 2 - CONSUMO SIN LIBRANZA
            -- MORA315
            -- =====================================================

            CASE
                WHEN p.id_modelo_pe = 2
                 AND rm.mora_max_3m BETWEEN 16 AND 30
                    THEN 1
                ELSE 0
            END AS mora315,

            -- =====================================================
            -- MODELO 3 - COMERCIAL PERSONA NATURAL
            -- MORTRIM
            -- =====================================================

            CASE
                WHEN p.id_modelo_pe = 3
                 AND rm.cantidad_mora_31_60_3m >= 1
                    THEN 1
                ELSE 0
            END AS mortrim,

            -- =====================================================
            -- MODELO 1 - CONSUMO CON LIBRANZA
            -- MORA3660
            -- =====================================================

            CASE
                WHEN p.id_modelo_pe = 1
                 AND rm.mora_max_36m >= 60
                 AND rm.mora_max_24m = 0
                    THEN 1
                ELSE 0
            END AS mora3660,

            -- =====================================================
            -- RESULTADOS DEL MODELO
            -- =====================================================

            CAST(NULL AS NUMERIC(18,10))
                AS z,

            CAST(NULL AS NUMERIC(18,10))
                AS puntaje,

            CAST(NULL AS VARCHAR(1))
                AS calificacion_modelo,

            -- CAL_NEW / EDAD_PE del proceso VB.
            -- Se conserva calificacion_modelo como resultado estadístico puro.
            CAST(NULL AS VARCHAR(1))
                AS calificacion_pe,

            -- =====================================================
            -- DEFAULT / DETERIORO / PI
            -- =====================================================

            CAST(NULL AS INTEGER)
                AS default_pe,

            CAST(NULL AS VARCHAR(1))
                AS edad_deterioro,

            CAST(NULL AS NUMERIC(8,4))
                AS pi,

                    -- =====================================================
                    -- VEA
                    -- =====================================================

                    CAST(NULL AS INTEGER)
                        AS tipo_doc_pe,

                    CAST(NULL AS NUMERIC(18,2))
                        AS vea,

                    -- =====================================================
                    -- PDI / PÉRDIDA ESPERADA
                    -- =====================================================

                    CAST(NULL AS NUMERIC(8,4))
                        AS pdi,

                    CAST(NULL AS NUMERIC(18,2))
                        AS perdida_valor,

                    CAST(NULL AS NUMERIC(18,10))
                        AS perdida_porce,

                    CAST(NULL AS NUMERIC(8,2))
                        AS porcentaje_capital,

                    CAST(NULL AS NUMERIC(8,2))
                        AS porcentaje_intereses,

                    CAST(NULL AS NUMERIC(8,2))
                        AS porcentaje_otros,

                    CAST(NULL AS NUMERIC(18,2))
                        AS deterioro_capital_pe,

                    CAST(NULL AS NUMERIC(18,2))
                        AS deterioro_intereses_pe,

                    CAST(NULL AS NUMERIC(18,2))
                        AS deterioro_otros_pe,

                    -- =====================================================
                    -- HOMOLOGACIÓN / ALINEACIÓN
                    -- =====================================================

                    CAST(NULL AS VARCHAR(1))
                        AS edad_homologada_individual,

                    CAST(NULL AS VARCHAR(1))
                        AS edad_contable_pe

                    FROM tmp_pe_poblacion p

                    INNER JOIN resumen_moras rm
                        ON rm.id_cierre_cartera_credito =
                           p.id_cierre_cartera_credito
        """;

        jdbcTemplate.execute(sql);

        Integer cantidad = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tmp_pe_variables
                """,
                Integer.class
        );

        return cantidad != null ? cantidad : 0;
    }


    // =========================================================
    // CONSULTAR VARIABLES
    //
    // Por ahora permite revisar el contenido generado durante
    // la misma transacción del proceso PE.
    // =========================================================

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

    // =========================================================
    // FINALIZAR PROCESO
    // =========================================================

    public void finalizarProceso(
            Integer idPeProceso
    ) {

        jdbcTemplate.update(
                """
                UPDATE cartera.pe_procesos
                SET
                    estado = 'FINALIZADO',
                    fecha_fin = CURRENT_TIMESTAMP
                WHERE id_pe_proceso = ?
                """,
                idPeProceso
        );
    }


    // =========================================================
    // MARCAR PROCESO CON ERROR
    // =========================================================

    public void marcarProcesoError(
            Integer idPeProceso
    ) {

        jdbcTemplate.update(
                """
                UPDATE cartera.pe_procesos
                SET
                    estado = 'ERROR',
                    fecha_fin = CURRENT_TIMESTAMP
                WHERE id_pe_proceso = ?
                """,
                idPeProceso
        );
    }

    // =========================================================
// CALCULAR Z, PUNTAJE Y CALIFICACIÓN
//
// MODELO 2 = CONSUMO SIN LIBRANZA
// MODELO 3 = COMERCIAL PERSONA NATURAL
//
// Los coeficientes se leen de:
// cartera.pe_modelos_coeficientes
//
// Los rangos de calificación se leen de:
// cartera.pe_modelos_calificaciones
// =========================================================

    public int calcularZPuntajeCalificacion() {

        // =====================================================
        // VALIDAR QUE NO EXISTA POBLACIÓN DE LIBRANZA
        // MIENTRAS NO ESTÉN IMPLEMENTADAS SUS VARIABLES
        // ADICIONALES.
        // =====================================================

        Integer cantidadLibranza = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tmp_pe_variables
                WHERE id_modelo_pe = 1
                """,
                Integer.class
        );

        if (cantidadLibranza != null && cantidadLibranza > 0) {
            throw new IllegalStateException(
                    "Existen " + cantidadLibranza +
                            " créditos de Consumo con Libranza. " +
                            "Falta implementar FE, VALCUOTA y FONDPLAZO."
            );
        }


        // =====================================================
        // CALCULAR Z
        // =====================================================

        String sqlZ = """
        WITH coeficientes AS
        (
            SELECT
                id_modelo_pe,

                MAX(coeficiente) FILTER (
                    WHERE variable = 'INTERCEPTO'
                ) AS intercepto,

                MAX(coeficiente) FILTER (
                    WHERE variable = 'EA'
                ) AS beta_ea,

                MAX(coeficiente) FILTER (
                    WHERE variable = 'MORA1230'
                ) AS beta_mora1230,

                MAX(coeficiente) FILTER (
                    WHERE variable = 'MORA1260'
                ) AS beta_mora1260,

                MAX(coeficiente) FILTER (
                    WHERE variable = 'SINMORA'
                ) AS beta_sinmora,

                MAX(coeficiente) FILTER (
                    WHERE variable = 'MORA2430N'
                ) AS beta_mora2430n,

                MAX(coeficiente) FILTER (
                    WHERE variable = 'MORA315'
                ) AS beta_mora315,

                MAX(coeficiente) FILTER (
                    WHERE variable = 'MORTRIM'
                ) AS beta_mortrim

            FROM cartera.pe_modelos_coeficientes

            WHERE activo = TRUE
              AND id_modelo_pe IN (2, 3)

            GROUP BY id_modelo_pe
        )

        UPDATE tmp_pe_variables v

        SET z =
            CASE

                -- =========================================
                -- 2. CONSUMO SIN LIBRANZA
                -- =========================================
                WHEN v.id_modelo_pe = 2 THEN

                      c.intercepto
                    + (v.ea         * c.beta_ea)
                    + (v.mora1230   * c.beta_mora1230)
                    + (v.mora1260   * c.beta_mora1260)
                    + (v.sinmora    * c.beta_sinmora)
                    + (v.mora2430n  * c.beta_mora2430n)
                    + (v.mora315    * c.beta_mora315)

                -- =========================================
                -- 3. COMERCIAL PERSONA NATURAL
                -- =========================================
                WHEN v.id_modelo_pe = 3 THEN

                      c.intercepto
                    + (v.ea         * c.beta_ea)
                    + (v.mora1230   * c.beta_mora1230)
                    + (v.mora1260   * c.beta_mora1260)
                    + (v.sinmora    * c.beta_sinmora)
                    + (v.mora2430n  * c.beta_mora2430n)
                    + (v.mortrim    * c.beta_mortrim)

                ELSE NULL

            END

        FROM coeficientes c

        WHERE c.id_modelo_pe =
              v.id_modelo_pe
        """;

        int actualizados =
                jdbcTemplate.update(sqlZ);


        // =====================================================
        // CALCULAR PUNTAJE
        //
        // puntaje = exp(z) / (1 + exp(z))
        //         = 1 / (1 + exp(-z))
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables
                SET puntaje =
                    1.0 / (1.0 + EXP(-z))
                WHERE z IS NOT NULL
                """
        );


        // =====================================================
        // CALIFICACIÓN A - B - C - D - E
        //
        // Se toma el primer límite superior que contenga
        // el puntaje.
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables v
    
                SET calificacion_modelo =
                    (
                        SELECT c.calificacion
    
                        FROM cartera.pe_modelos_calificaciones c
    
                        WHERE c.id_modelo_pe =
                              v.id_modelo_pe
    
                          AND c.activo = TRUE
    
                          AND v.puntaje <=
                              c.limite_superior
    
                        ORDER BY
                            c.limite_superior
    
                        LIMIT 1
                    )
    
                WHERE v.puntaje IS NOT NULL
                """
        );


        // =====================================================
        // VALIDAR RESULTADO
        // =====================================================

        Integer incompletos = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tmp_pe_variables
                WHERE id_modelo_pe IN (2, 3)
                  AND (
                        z IS NULL
                        OR puntaje IS NULL
                        OR calificacion_modelo IS NULL
                      )
                """,
                Integer.class
        );

        if (incompletos != null && incompletos > 0) {
            throw new IllegalStateException(
                    "Existen " + incompletos +
                            " créditos sin Z, puntaje o calificación."
            );
        }

        return actualizados;
    }

    // =========================================================
// CALCULAR DEFAULT, EDAD DE DETERIORO Y PI
//
// Replica la lógica VB:
//
// DEFAULT = dias_mora_actual > dias_default
//
// Si DEFAULT = 1:
//     calificacion_modelo = E
//     edad_deterioro      = E
//     PI                  = 100
//
// Si DEFAULT = 0:
//     edad_deterioro = mayor entre:
//         edad_de_mora
//         edad_de_riesgo
//         calificacion_modelo
//
//     PI = tabla parametrizada según:
//         tipo entidad = 3
//         modelo
//         edad_deterioro
// =========================================================

    public int calcularDefaultEdadDeterioroYPi() {

        // =====================================================
        // NORMALIZAR EDADES
        //
        // VB:
        // NULL -> A
        // F    -> E
        // =====================================================

        jdbcTemplate.update("""
            UPDATE tmp_pe_variables
            SET
                edad_de_mora =
                    CASE
                        WHEN edad_de_mora IS NULL
                          OR TRIM(edad_de_mora) = ''
                            THEN 'A'
                        WHEN edad_de_mora = 'F'
                            THEN 'E'
                        ELSE edad_de_mora
                    END,

                edad_de_riesgo =
                    CASE
                        WHEN edad_de_riesgo IS NULL
                          OR TRIM(edad_de_riesgo) = ''
                            THEN 'A'
                        WHEN edad_de_riesgo = 'F'
                            THEN 'E'
                        ELSE edad_de_riesgo
                    END
            """);

        // =====================================================
        // DEFAULT Y CAL_NEW / EDAD_PE
        //
        // VB:
        // DEFAULT = Morosidad > dias_default
        //
        // Si DEFAULT = 1:
        //     CAL_NEW = E
        // Si DEFAULT = 0:
        //     CAL_NEW = calificación estadística
        //
        // calificacion_modelo NO se sobreescribe.
        // =====================================================

        jdbcTemplate.update("""
            UPDATE tmp_pe_variables v
            SET
                default_pe =
                    CASE
                        WHEN v.dias_mora_actual > m.dias_default
                            THEN 1
                        ELSE 0
                    END,

                calificacion_pe =
                    CASE
                        WHEN v.dias_mora_actual > m.dias_default
                            THEN 'E'
                        ELSE v.calificacion_modelo
                    END

            FROM cartera.pe_modelos m

            WHERE m.id_modelo_pe = v.id_modelo_pe
              AND m.activo = TRUE
            """);

        // =====================================================
        // EDAD_DETE
        // =====================================================

        jdbcTemplate.update("""
            UPDATE tmp_pe_variables
            SET edad_deterioro =
                CASE
                    WHEN default_pe = 1
                        THEN 'E'

                    ELSE
                        CASE
                            GREATEST(
                                CASE edad_de_mora
                                    WHEN 'A' THEN 1
                                    WHEN 'B' THEN 2
                                    WHEN 'C' THEN 3
                                    WHEN 'D' THEN 4
                                    WHEN 'E' THEN 5
                                    ELSE 1
                                END,

                                CASE edad_de_riesgo
                                    WHEN 'A' THEN 1
                                    WHEN 'B' THEN 2
                                    WHEN 'C' THEN 3
                                    WHEN 'D' THEN 4
                                    WHEN 'E' THEN 5
                                    ELSE 1
                                END,

                                CASE calificacion_pe
                                    WHEN 'A' THEN 1
                                    WHEN 'B' THEN 2
                                    WHEN 'C' THEN 3
                                    WHEN 'D' THEN 4
                                    WHEN 'E' THEN 5
                                    ELSE 1
                                END
                            )
                            WHEN 1 THEN 'A'
                            WHEN 2 THEN 'B'
                            WHEN 3 THEN 'C'
                            WHEN 4 THEN 'D'
                            WHEN 5 THEN 'E'
                        END
                END
            """);

        // =====================================================
        // PI
        // =====================================================

        jdbcTemplate.update("""
            UPDATE tmp_pe_variables
            SET pi = 100.0000
            WHERE default_pe = 1
            """);

        jdbcTemplate.update("""
            UPDATE tmp_pe_variables v
            SET pi = p.porcentaje_pi

            FROM cartera.pe_modelos_pi p

            WHERE p.id_tipo_entidad = 3
              AND p.id_modelo_pe = v.id_modelo_pe
              AND p.calificacion = v.edad_deterioro
              AND p.activo = TRUE
              AND v.default_pe = 0
            """);

        // =====================================================
        // VALIDAR RESULTADO
        // =====================================================

        Integer incompletos = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tmp_pe_variables
                WHERE default_pe IS NULL
                   OR calificacion_pe IS NULL
                   OR edad_deterioro IS NULL
                   OR pi IS NULL
                """,
                Integer.class
        );

        if (incompletos != null && incompletos > 0) {
            throw new IllegalStateException(
                    "Existen " + incompletos +
                            " créditos sin DEFAULT, CAL_NEW, edad de deterioro o PI."
            );
        }

        Integer cantidad = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tmp_pe_variables
                """,
                Integer.class
        );

        return cantidad != null ? cantidad : 0;
    }

    public int calcularVea(Integer idCierreCartera) {

        String sql = """
        UPDATE tmp_pe_variables v
        SET
            tipo_doc_pe =
                CASE
                    WHEN EXTRACT(YEAR FROM v.fecha_desembolso) =
                         EXTRACT(YEAR FROM c.fecha_corte)
                     AND EXTRACT(MONTH FROM v.fecha_desembolso) =
                         EXTRACT(MONTH FROM c.fecha_corte)
                    THEN 1
                    ELSE 0
                END,

            vea =
                CASE
                    WHEN EXTRACT(YEAR FROM v.fecha_desembolso) =
                         EXTRACT(YEAR FROM c.fecha_corte)
                     AND EXTRACT(MONTH FROM v.fecha_desembolso) =
                         EXTRACT(MONTH FROM c.fecha_corte)
                    THEN 0

                    ELSE GREATEST(
                        0,
                          COALESCE(v.saldo_actual, 0)
                        + COALESCE(v.saldo_intereses_causados, 0)
                        + COALESCE(v.valor_costas_judiciales, 0)
                        - COALESCE(v.valor_aportes_credito, 0)
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

        return actualizados;
    }

    public int calcularPdiYPerdida() {

        // =========================================================
        // 1. CALCULAR PDI
        //
        // Replica sacar_probabilidad_pdi() del VB.
        // =========================================================

        jdbcTemplate.update("""
        UPDATE tmp_pe_variables v

        SET pdi =
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

        WHERE p.id_modelo_pe = v.id_modelo_pe
          AND p.codigo_garantia_credito =
              v.codigo_garantia_credito
          AND p.activo = TRUE
        """);


        // =========================================================
        // VALIDAR QUE TODOS TENGAN PDI
        // Equivale a evitar VALOR_RTA = -1 del VB.
        // =========================================================

        Integer sinPdi = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tmp_pe_variables
                WHERE pdi IS NULL
                """,
                Integer.class
        );

        if (sinPdi != null && sinPdi > 0) {
            throw new IllegalStateException(
                    "Existen " + sinPdi +
                            " créditos sin parametrización PDI."
            );
        }


        // =========================================================
        // 2. CALCULAR PÉRDIDA ESPERADA
        //
        // VB:
        //
        // PERDIDA_VALOR =
        // ROUND(
        //   (PROBABILIDAD / 100)
        //   * VALOR_EXPUESTO
        //   * (PDI / 100),
        //   0
        // )
        // =========================================================

        jdbcTemplate.update("""
        UPDATE tmp_pe_variables
        SET perdida_valor =
            ROUND(
                (pi / 100.0)
                * vea
                * (pdi / 100.0),
                0
            )
        """);


        // =========================================================
        // 3. PORCENTAJE DE PÉRDIDA
        //
        // En VB NO se multiplica por 100:
        //
        // PERDIDA_PORCE = PERDIDA_VALOR / VALOR_EXPUESTO
        // =========================================================

        jdbcTemplate.update("""
        UPDATE tmp_pe_variables
        SET perdida_porce =
            CASE
                WHEN vea <> 0
                    THEN perdida_valor / vea
                ELSE 0
            END
        """);


        // =========================================================
        // 4. DISTRIBUCIÓN ENTRE CAPITAL / INTERESES / OTROS
        //
        // Otros saldos en nuestro modelo =
        // valor_costas_judiciales
        // =========================================================

        jdbcTemplate.update("""
        UPDATE tmp_pe_variables
        SET
            porcentaje_capital =
                CASE
                    WHEN (
                          COALESCE(saldo_actual, 0)
                        + COALESCE(saldo_intereses_causados, 0)
                        + COALESCE(valor_costas_judiciales, 0)
                    ) <> 0
                    THEN ROUND(
                        (
                            saldo_actual * 100.0
                        ) /
                        (
                              COALESCE(saldo_actual, 0)
                            + COALESCE(saldo_intereses_causados, 0)
                            + COALESCE(valor_costas_judiciales, 0)
                        ),
                        2
                    )
                    ELSE 0
                END,

            porcentaje_intereses =
                CASE
                    WHEN (
                          COALESCE(saldo_actual, 0)
                        + COALESCE(saldo_intereses_causados, 0)
                        + COALESCE(valor_costas_judiciales, 0)
                    ) <> 0
                    THEN ROUND(
                        (
                            saldo_intereses_causados * 100.0
                        ) /
                        (
                              COALESCE(saldo_actual, 0)
                            + COALESCE(saldo_intereses_causados, 0)
                            + COALESCE(valor_costas_judiciales, 0)
                        ),
                        2
                    )
                    ELSE 0
                END,

            porcentaje_otros =
                CASE
                    WHEN (
                          COALESCE(saldo_actual, 0)
                        + COALESCE(saldo_intereses_causados, 0)
                        + COALESCE(valor_costas_judiciales, 0)
                    ) <> 0
                    THEN ROUND(
                        (
                            valor_costas_judiciales * 100.0
                        ) /
                        (
                              COALESCE(saldo_actual, 0)
                            + COALESCE(saldo_intereses_causados, 0)
                            + COALESCE(valor_costas_judiciales, 0)
                        ),
                        2
                    )
                    ELSE 0
                END
        """);


        // =========================================================
        // 5. DISTRIBUIR PÉRDIDA
        //
        // VB:
        //
        // CAL_DETE_CAP = ROUND(PE * %CAP / 100, 0)
        // CAL_DETE_INT = ROUND(PE * %INT / 100, 0)
        //
        // OTROS queda como diferencia para garantizar que:
        //
        // CAP + INT + OTROS = PE
        // =========================================================

        jdbcTemplate.update("""
        UPDATE tmp_pe_variables
        SET
            deterioro_capital_pe =
                ROUND(
                    perdida_valor
                    * (porcentaje_capital / 100.0),
                    0
                ),

            deterioro_intereses_pe =
                ROUND(
                    perdida_valor
                    * (porcentaje_intereses / 100.0),
                    0
                )
        """);


        jdbcTemplate.update("""
        UPDATE tmp_pe_variables
        SET deterioro_otros_pe =
              perdida_valor
            - deterioro_capital_pe
            - deterioro_intereses_pe
        """);


        // =========================================================
        // VALIDACIÓN FINAL
        // =========================================================

        Integer incompletos = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tmp_pe_variables
                WHERE pdi IS NULL
                   OR perdida_valor IS NULL
                   OR perdida_porce IS NULL
                   OR deterioro_capital_pe IS NULL
                   OR deterioro_intereses_pe IS NULL
                   OR deterioro_otros_pe IS NULL
                """,
                Integer.class
        );

        if (incompletos != null && incompletos > 0) {
            throw new IllegalStateException(
                    "Existen " + incompletos +
                            " créditos con cálculo PDI/PE incompleto."
            );
        }

        Integer cantidad = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tmp_pe_variables
                """,
                Integer.class
        );

        return cantidad != null ? cantidad : 0;
    }

    public int calcularHomologacionYAlineacion() {

        // =========================================================
        // 1. HOMOLOGACIÓN INDIVIDUAL
        //
        // VB:
        //
        // Si EDAD_EVAL >= EDAD_PE:
        //     EDAD_HOMO = EDAD_EVAL
        //
        // Si EDAD_EVAL < EDAD_PE:
        //     EDAD_HOMO = SACAR_EDAD_HOMOLOGACION(...)
        //
        // EDAD_EVAL = edad_de_riesgo
        // EDAD_PE   = calificacion_pe
        // =========================================================

        jdbcTemplate.update("""
            UPDATE tmp_pe_variables v

            SET edad_homologada_individual =
                CASE
                    WHEN
                        CASE v.edad_de_riesgo
                            WHEN 'A' THEN 1
                            WHEN 'B' THEN 2
                            WHEN 'C' THEN 3
                            WHEN 'D' THEN 4
                            WHEN 'E' THEN 5
                            ELSE 1
                        END
                        >=
                        CASE v.calificacion_pe
                            WHEN 'A' THEN 1
                            WHEN 'B' THEN 2
                            WHEN 'C' THEN 3
                            WHEN 'D' THEN 4
                            WHEN 'E' THEN 5
                            ELSE 1
                        END

                    THEN v.edad_de_riesgo

                    ELSE
                        CASE v.calificacion_pe

                            WHEN 'A' THEN 'A'

                            WHEN 'B' THEN
                                CASE
                                    WHEN v.dias_mora_actual <= COALESCE(
                                        (
                                            SELECT h.mora_hasta
                                            FROM cartera.pe_modelos_homologacion h
                                            WHERE h.id_modelo_pe = v.id_modelo_pe
                                              AND h.calificacion_modelo = 'B'
                                              AND h.calificacion_homologada = 'A'
                                              AND h.activo = TRUE
                                            ORDER BY h.mora_hasta DESC
                                            LIMIT 1
                                        ),
                                        30
                                    )
                                    THEN 'A'
                                    ELSE 'B'
                                END

                            WHEN 'C' THEN
                                CASE
                                    WHEN v.dias_mora_actual <= COALESCE(
                                        (
                                            SELECT h.mora_hasta
                                            FROM cartera.pe_modelos_homologacion h
                                            WHERE h.id_modelo_pe = v.id_modelo_pe
                                              AND h.calificacion_modelo = 'C'
                                              AND h.calificacion_homologada = 'B'
                                              AND h.activo = TRUE
                                            ORDER BY h.mora_hasta DESC
                                            LIMIT 1
                                        ),
                                        30
                                    )
                                    THEN 'B'
                                    ELSE 'C'
                                END

                            WHEN 'D' THEN 'C'

                            WHEN 'E' THEN
                                CASE
                                    WHEN v.default_pe = 0
                                        THEN 'C'

                                    WHEN v.dias_mora_actual <= COALESCE(
                                        (
                                            SELECT h.mora_hasta
                                            FROM cartera.pe_modelos_homologacion h
                                            WHERE h.id_modelo_pe = v.id_modelo_pe
                                              AND h.calificacion_modelo = 'E'
                                              AND h.calificacion_homologada = 'D'
                                              AND h.activo = TRUE
                                            ORDER BY h.mora_hasta DESC
                                            LIMIT 1
                                        ),
                                        v.dias_mora_actual
                                    )
                                        THEN 'D'

                                    ELSE 'E'
                                END
                        END
                END
            """);

        // =========================================================
        // 2. ALINEACIÓN POR DOCUMENTO + MODELO
        // =========================================================

        jdbcTemplate.update("""
            WITH maxima_edad AS
            (
                SELECT
                    documento,
                    id_modelo_pe,

                    MAX(
                        CASE edad_homologada_individual
                            WHEN 'A' THEN 1
                            WHEN 'B' THEN 2
                            WHEN 'C' THEN 3
                            WHEN 'D' THEN 4
                            WHEN 'E' THEN 5
                            ELSE 1
                        END
                    ) AS nivel_maximo

                FROM tmp_pe_variables

                GROUP BY
                    documento,
                    id_modelo_pe
            )

            UPDATE tmp_pe_variables v

            SET edad_contable_pe =
                CASE m.nivel_maximo
                    WHEN 1 THEN 'A'
                    WHEN 2 THEN 'B'
                    WHEN 3 THEN 'C'
                    WHEN 4 THEN 'D'
                    WHEN 5 THEN 'E'
                END

            FROM maxima_edad m

            WHERE m.documento = v.documento
              AND m.id_modelo_pe = v.id_modelo_pe
            """);

        // =========================================================
        // VALIDACIÓN
        // =========================================================

        Integer incompletos = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tmp_pe_variables
                WHERE edad_homologada_individual IS NULL
                   OR edad_contable_pe IS NULL
                """,
                Integer.class
        );

        if (incompletos != null && incompletos > 0) {
            throw new IllegalStateException(
                    "Existen " + incompletos +
                            " créditos sin homologación/alineación."
            );
        }

        Integer cantidad = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tmp_pe_variables
                """,
                Integer.class
        );

        return cantidad != null ? cantidad : 0;
    }

    public int persistirResultadosPe(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        String sql = """
        UPDATE cartera.cierres_cartera_resultados r

        SET
            edad_de_pe =
                v.calificacion_pe,

            edad_de_homologacion =
                v.edad_homologada_individual,

            edad_contable =
                v.edad_contable_pe,

            vea =
                v.vea,

            pi =
                v.pi,

            pdi =
                v.pdi,

            perdida_esperada =
                v.perdida_valor,

            deterioro_capital =
                v.deterioro_capital_pe,

            deterioro_intereses =
                v.deterioro_intereses_pe,

            deterioro_otros =
                v.deterioro_otros_pe,

            fecha_calculo =
                CURRENT_TIMESTAMP,

            fk_seguridad_edicion =
                ?,

            fecha_edicion =
                CURRENT_TIMESTAMP

        FROM tmp_pe_variables v

        WHERE r.id_cierre_cartera_credito =
              v.id_cierre_cartera_credito

          AND r.id_cierre_cartera =
              ?
        """;

        int actualizados =
                jdbcTemplate.update(
                        sql,
                        idUsuario,
                        idCierreCartera
                );

        Integer incompletos = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
    
                FROM tmp_pe_variables v
    
                LEFT JOIN cartera.cierres_cartera_resultados r
                    ON r.id_cierre_cartera_credito =
                       v.id_cierre_cartera_credito
    
                   AND r.id_cierre_cartera = ?
    
                WHERE r.id_cierre_cartera_resultado IS NULL
                """,
                Integer.class,
                idCierreCartera
        );

        if (incompletos != null && incompletos > 0) {
            throw new IllegalStateException(
                    "Existen " + incompletos +
                            " créditos PE sin registro en cierres_cartera_resultados."
            );
        }

        return actualizados;
    }

}
