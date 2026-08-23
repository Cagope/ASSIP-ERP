package co.assip.erp.cartera.cierremensual.anexo2;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class Anexo2ModeloRepository {

    private final JdbcTemplate jdbcTemplate;

    // Modelo estadístico PE: Z, puntaje, calificación, default y PI.

    public int calcularZPuntajeCalificacion() {

        // =====================================================
        // 1. VALIDAR MODELO CON LIBRANZA
        //
        // El modelo 1 todavía requiere:
        //
        // FE
        // VALCUOTA
        // FONDPLAZO
        // =====================================================

        Integer cantidadLibranza =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM tmp_pe_variables
                        WHERE id_modelo_pe = 1
                        """,
                        Integer.class
                );

        if (cantidadLibranza != null
                && cantidadLibranza > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + cantidadLibranza
                            + " créditos de Consumo con Libranza. "
                            + "Falta implementar FE, VALCUOTA y FONDPLAZO."
            );
        }

        // =====================================================
        // 2. CARGAR BETAS, CALCULAR APORTES Y Z
        //
        // MODELO 2:
        //
        // Z =
        //   intercepto
        // + EA
        // + MORA1230
        // + MORA1260
        // + SINMORA
        // + MORA2430N
        // + MORA315
        //
        // MODELO 3:
        //
        // Z =
        //   intercepto
        // + EA
        // + MORA1230
        // + MORA1260
        // + SINMORA
        // + MORA2430N
        // + MORTRIM
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
                WHERE variable = 'FE'
            ) AS beta_fe,

            MAX(coeficiente) FILTER (
                WHERE variable = 'VALCUOTA'
            ) AS beta_valcuota,

            MAX(coeficiente) FILTER (
                WHERE variable = 'FONDPLAZO'
            ) AS beta_fondplazo,

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
            ) AS beta_mortrim,

            MAX(coeficiente) FILTER (
                WHERE variable = 'MORA3660'
            ) AS beta_mora3660

        FROM cartera.pe_modelos_coeficientes

        WHERE activo = TRUE
          AND id_modelo_pe IN (2, 3)

        GROUP BY
            id_modelo_pe
    )

    UPDATE tmp_pe_variables v

    SET
        -- =================================================
        -- BETAS CONGELADOS
        -- =================================================

        beta_intercepto =
            c.intercepto,

        beta_ea =
            c.beta_ea,

        beta_fe =
            c.beta_fe,

        beta_valcuota =
            c.beta_valcuota,

        beta_fondplazo =
            c.beta_fondplazo,

        beta_mora1230 =
            c.beta_mora1230,

        beta_mora1260 =
            c.beta_mora1260,

        beta_sinmora =
            c.beta_sinmora,

        beta_mora2430n =
            c.beta_mora2430n,

        beta_mora315 =
            CASE
                WHEN v.id_modelo_pe = 2
                    THEN c.beta_mora315
                ELSE NULL
            END,

        beta_mortrim =
            CASE
                WHEN v.id_modelo_pe = 3
                    THEN c.beta_mortrim
                ELSE NULL
            END,

        beta_mora3660 =
            NULL,

        -- =================================================
        -- APORTE INDIVIDUAL A Z
        -- =================================================

        aporte_z_intercepto =
            c.intercepto,

        aporte_z_ea =
            v.ea
            * c.beta_ea,

        aporte_z_fe =
            NULL,

        aporte_z_valcuota =
            NULL,

        aporte_z_fondplazo =
            NULL,

        aporte_z_mora1230 =
            v.mora1230
            * c.beta_mora1230,

        aporte_z_mora1260 =
            v.mora1260
            * c.beta_mora1260,

        aporte_z_sinmora =
            v.sinmora
            * c.beta_sinmora,

        aporte_z_mora2430n =
            v.mora2430n
            * c.beta_mora2430n,

        aporte_z_mora315 =
            CASE
                WHEN v.id_modelo_pe = 2
                    THEN v.mora315
                         * c.beta_mora315
                ELSE NULL
            END,

        aporte_z_mortrim =
            CASE
                WHEN v.id_modelo_pe = 3
                    THEN v.mortrim
                         * c.beta_mortrim
                ELSE NULL
            END,

        aporte_z_mora3660 =
            NULL,

        -- =================================================
        -- Z
        -- =================================================

        z =
            CASE

                -- =========================================
                -- MODELO 2
                -- CONSUMO SIN LIBRANZA
                -- =========================================

                WHEN v.id_modelo_pe = 2 THEN

                      c.intercepto

                    + (
                        v.ea
                        * c.beta_ea
                      )

                    + (
                        v.mora1230
                        * c.beta_mora1230
                      )

                    + (
                        v.mora1260
                        * c.beta_mora1260
                      )

                    + (
                        v.sinmora
                        * c.beta_sinmora
                      )

                    + (
                        v.mora2430n
                        * c.beta_mora2430n
                      )

                    + (
                        v.mora315
                        * c.beta_mora315
                      )

                -- =========================================
                -- MODELO 3
                -- COMERCIAL PERSONA NATURAL
                -- =========================================

                WHEN v.id_modelo_pe = 3 THEN

                      c.intercepto

                    + (
                        v.ea
                        * c.beta_ea
                      )

                    + (
                        v.mora1230
                        * c.beta_mora1230
                      )

                    + (
                        v.mora1260
                        * c.beta_mora1260
                      )

                    + (
                        v.sinmora
                        * c.beta_sinmora
                      )

                    + (
                        v.mora2430n
                        * c.beta_mora2430n
                      )

                    + (
                        v.mortrim
                        * c.beta_mortrim
                      )

                ELSE NULL

            END

    FROM coeficientes c

    WHERE c.id_modelo_pe =
          v.id_modelo_pe
    """;

        int actualizados =
                jdbcTemplate.update(
                        sqlZ
                );

        // =====================================================
        // 3. VALIDAR BETAS OBLIGATORIOS
        //
        // Si falta un coeficiente que participa en la ecuación,
        // no debemos continuar con un resultado incompleto.
        // =====================================================

        Integer betasIncompletos =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
    
                        FROM tmp_pe_variables
    
                        WHERE
                            beta_intercepto IS NULL
                            OR beta_ea IS NULL
                            OR beta_mora1230 IS NULL
                            OR beta_mora1260 IS NULL
                            OR beta_sinmora IS NULL
                            OR beta_mora2430n IS NULL
    
                            OR (
                                id_modelo_pe = 2
                                AND beta_mora315 IS NULL
                            )
    
                            OR (
                                id_modelo_pe = 3
                                AND beta_mortrim IS NULL
                            )
                        """,
                        Integer.class
                );

        if (betasIncompletos != null
                && betasIncompletos > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + betasIncompletos
                            + " créditos PE con coeficientes "
                            + "incompletos para calcular Z."
            );
        }

        // =====================================================
        // 4. CALCULAR PUNTAJE
        //
        // puntaje =
        //
        // 1
        // -----------------
        // 1 + EXP(-Z)
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables
    
                SET puntaje =
                    1.0 / (
                        1.0 + EXP(-z)
                    )
    
                WHERE z IS NOT NULL
                """
        );

        // =====================================================
        // 5. CALIFICACIÓN A - B - C - D - E
        //
        // Se toma el primer límite superior que contenga
        // el puntaje.
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables v
    
                SET calificacion_modelo =
                    (
                        SELECT
                            c.calificacion
    
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
        // 6. VALIDAR RESULTADO
        // =====================================================

        Integer incompletos =
                jdbcTemplate.queryForObject(
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

        if (incompletos != null
                && incompletos > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + incompletos
                            + " créditos sin Z, puntaje "
                            + "o calificación."
            );
        }

        return actualizados;
    }


    public int calcularDefaultEdadDeterioroYPi() {

        // =====================================================
        // 1. NORMALIZAR EDADES
        //
        // NULL / VACÍO -> A
        // F            -> E
        // =====================================================

        jdbcTemplate.update(
                """
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
                """
        );

        // =====================================================
        // 2. CONGELAR PARÁMETROS DEL MODELO
        //
        // dias_default_modelo:
        //     se toma de cartera.pe_modelos.
        //
        // tipo_entidad_pe:
        //     actualmente el motor trabaja con:
        //
        //     id_tipo_entidad = 3
        //
        // Este valor queda explícitamente guardado para
        // trazabilidad del cálculo.
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables v
    
                SET
                    dias_default_modelo =
                        m.dias_default,
    
                    tipo_entidad_pe =
                        3
    
                FROM cartera.pe_modelos m
    
                WHERE m.id_modelo_pe =
                      v.id_modelo_pe
    
                  AND m.activo = TRUE
                """
        );

        // =====================================================
        // 3. VALIDAR PARÁMETROS DEL MODELO
        // =====================================================

        Integer modelosSinParametros =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
    
                        FROM tmp_pe_variables
    
                        WHERE dias_default_modelo IS NULL
                           OR tipo_entidad_pe IS NULL
                        """,
                        Integer.class
                );

        if (modelosSinParametros != null
                && modelosSinParametros > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + modelosSinParametros
                            + " créditos PE sin parámetros "
                            + "de default o tipo de entidad."
            );
        }

        // =====================================================
        // 4. CALCULAR DEFAULT Y CALIFICACIÓN PE
        //
        // DEFAULT:
        //
        // dias_mora_actual > dias_default_modelo
        //
        // Si existe incumplimiento:
        //
        // calificacion_pe = E
        //
        // Si no:
        //
        // calificacion_pe = calificacion_modelo
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables
    
                SET
                    default_pe =
                        CASE
                            WHEN dias_mora_actual >
                                 dias_default_modelo
                                THEN 1
    
                            ELSE 0
                        END,
    
                    calificacion_pe =
                        CASE
                            WHEN dias_mora_actual >
                                 dias_default_modelo
                                THEN 'E'
    
                            ELSE calificacion_modelo
                        END
                """
        );

        // =====================================================
        // 5. CALCULAR EDAD DE DETERIORO
        //
        // Si DEFAULT:
        //
        //     E
        //
        // En caso contrario:
        //
        // peor edad entre:
        //
        //     edad_de_mora
        //     edad_de_riesgo
        //     calificacion_pe
        // =====================================================

        jdbcTemplate.update(
                """
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
                """
        );

        // =====================================================
        // 6. CONGELAR CALIFICACIÓN BASE UTILIZADA PARA PI
        //
        // Para créditos en incumplimiento:
        //
        //     edad_deterioro = E
        //     PI = 100
        //
        // Para los demás:
        //
        //     la PI se consulta usando edad_deterioro.
        //
        // Por tanto dejamos congelado ese valor como
        // calificacion_base_pi.
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables
    
                SET calificacion_base_pi =
                    edad_deterioro
                """
        );

        // =====================================================
        // 7. PI PARA INCUMPLIDOS
        //
        // DEFAULT = 1
        // PI      = 100 %
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables
    
                SET pi =
                    100.0000
    
                WHERE default_pe = 1
                """
        );

        // =====================================================
        // 8. PI PARA NO INCUMPLIDOS
        //
        // La selección se realiza por:
        //
        // - tipo entidad
        // - modelo PE
        // - calificación base PI
        //
        // Ya NO dejamos quemado aquí:
        //
        // p.id_tipo_entidad = 3
        //
        // Utilizamos el valor que acabamos de congelar en:
        //
        // v.tipo_entidad_pe
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables v
    
                SET pi =
                    p.porcentaje_pi
    
                FROM cartera.pe_modelos_pi p
    
                WHERE p.id_tipo_entidad =
                      v.tipo_entidad_pe
    
                  AND p.id_modelo_pe =
                      v.id_modelo_pe
    
                  AND p.calificacion =
                      v.calificacion_base_pi
    
                  AND p.activo = TRUE
    
                  AND v.default_pe = 0
                """
        );

        // =====================================================
        // 9. VALIDAR RESULTADO
        // =====================================================

        Integer incompletos =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
    
                        FROM tmp_pe_variables
    
                        WHERE default_pe IS NULL
                           OR calificacion_pe IS NULL
                           OR edad_deterioro IS NULL
                           OR dias_default_modelo IS NULL
                           OR tipo_entidad_pe IS NULL
                           OR calificacion_base_pi IS NULL
                           OR pi IS NULL
                        """,
                        Integer.class
                );

        if (incompletos != null
                && incompletos > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + incompletos
                            + " créditos sin cálculo completo "
                            + "de DEFAULT, calificación PE, "
                            + "edad de deterioro o PI."
            );
        }

        // =====================================================
        // 10. VALIDAR RANGO DE PI
        // =====================================================

        Integer piInvalidas =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
    
                        FROM tmp_pe_variables
    
                        WHERE pi < 0
                           OR pi > 100
                        """,
                        Integer.class
                );

        if (piInvalidas != null
                && piInvalidas > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + piInvalidas
                            + " créditos con una PI fuera "
                            + "del rango permitido 0 - 100."
            );
        }

        // =====================================================
        // 11. RESULTADO
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
