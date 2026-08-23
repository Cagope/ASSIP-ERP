package co.assip.erp.cartera.cierremensual.anexo2;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class Anexo2HomologacionRepository {

    private final JdbcTemplate jdbcTemplate;

    // Homologación individual y alineación PE.

    public int calcularHomologacionYAlineacion() {

        // =====================================================
        // 1. CONGELAR DÍAS DE MORA UTILIZADOS EN HOMOLOGACIÓN
        //
        // Por ahora se conserva exactamente la lógica actual:
        //
        // dias_mora_homologacion =
        //     dias_mora_actual
        //
        // Esto permite registrar posteriormente qué mora
        // intervino efectivamente en la homologación.
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables
    
                SET dias_mora_homologacion =
                    dias_mora_actual
                """
        );


        // =====================================================
        // 2. HOMOLOGACIÓN INDIVIDUAL
        //
        // REGLA ACTUAL:
        //
        // Si edad_de_riesgo >= calificacion_pe:
        //
        //     edad_homologada_individual =
        //         edad_de_riesgo
        //
        // Si edad_de_riesgo < calificacion_pe:
        //
        //     se aplica homologación según:
        //
        //     - modelo PE
        //     - calificación PE
        //     - días de mora de homologación
        //     - parametrización
        //
        // =====================================================

        jdbcTemplate.update(
                """
                UPDATE tmp_pe_variables v
    
                SET edad_homologada_individual =
                    CASE
    
                        -- =====================================
                        -- SI LA EDAD DE RIESGO YA ES IGUAL
                        -- O PEOR QUE LA CALIFICACIÓN PE,
                        -- SE CONSERVA.
                        -- =====================================
    
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
    
    
                        -- =====================================
                        -- SI PE ES PEOR, APLICAR HOMOLOGACIÓN
                        -- =====================================
    
                        ELSE
                            CASE v.calificacion_pe
    
                                -- =============================
                                -- A -> A
                                -- =============================
    
                                WHEN 'A'
                                    THEN 'A'
    
    
                                -- =============================
                                -- B
                                --
                                -- mora <= límite -> A
                                -- mora >  límite -> B
                                -- =============================
    
                                WHEN 'B'
                                    THEN
                                        CASE
                                            WHEN v.dias_mora_homologacion
                                                 <=
                                                 COALESCE(
                                                     (
                                                         SELECT
                                                             h.mora_hasta
    
                                                         FROM cartera.pe_modelos_homologacion h
    
                                                         WHERE h.id_modelo_pe =
                                                               v.id_modelo_pe
    
                                                           AND h.calificacion_modelo =
                                                               'B'
    
                                                           AND h.calificacion_homologada =
                                                               'A'
    
                                                           AND h.activo = TRUE
    
                                                         ORDER BY
                                                             h.mora_hasta DESC
    
                                                         LIMIT 1
                                                     ),
                                                     30
                                                 )
    
                                                THEN 'A'
    
                                            ELSE 'B'
                                        END
    
    
                                -- =============================
                                -- C
                                --
                                -- mora <= límite -> B
                                -- mora >  límite -> C
                                -- =============================
    
                                WHEN 'C'
                                    THEN
                                        CASE
                                            WHEN v.dias_mora_homologacion
                                                 <=
                                                 COALESCE(
                                                     (
                                                         SELECT
                                                             h.mora_hasta
    
                                                         FROM cartera.pe_modelos_homologacion h
    
                                                         WHERE h.id_modelo_pe =
                                                               v.id_modelo_pe
    
                                                           AND h.calificacion_modelo =
                                                               'C'
    
                                                           AND h.calificacion_homologada =
                                                               'B'
    
                                                           AND h.activo = TRUE
    
                                                         ORDER BY
                                                             h.mora_hasta DESC
    
                                                         LIMIT 1
                                                     ),
                                                     30
                                                 )
    
                                                THEN 'B'
    
                                            ELSE 'C'
                                        END
    
    
                                -- =============================
                                -- D -> C
                                -- =============================
    
                                WHEN 'D'
                                    THEN 'C'
    
    
                                -- =============================
                                -- E
                                --
                                -- Si NO está incumplido:
                                --     C
                                --
                                -- Si está incumplido:
                                --
                                --     hasta límite -> D
                                --     después      -> E
                                -- =============================
    
                                WHEN 'E'
                                    THEN
                                        CASE
    
                                            WHEN v.default_pe = 0
                                                THEN 'C'
    
                                            WHEN v.dias_mora_homologacion
                                                 <=
                                                 COALESCE(
                                                     (
                                                         SELECT
                                                             h.mora_hasta
    
                                                         FROM cartera.pe_modelos_homologacion h
    
                                                         WHERE h.id_modelo_pe =
                                                               v.id_modelo_pe
    
                                                           AND h.calificacion_modelo =
                                                               'E'
    
                                                           AND h.calificacion_homologada =
                                                               'D'
    
                                                           AND h.activo = TRUE
    
                                                         ORDER BY
                                                             h.mora_hasta DESC
    
                                                         LIMIT 1
                                                     ),
                                                     v.dias_mora_homologacion
                                                 )
    
                                                THEN 'D'
    
                                            ELSE 'E'
                                        END
    
                            END
                    END
                """
        );


        // =====================================================
        // 3. VALIDAR HOMOLOGACIÓN INDIVIDUAL
        // =====================================================

        Integer sinHomologacion =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
    
                        FROM tmp_pe_variables
    
                        WHERE dias_mora_homologacion IS NULL
                           OR edad_homologada_individual IS NULL
                        """,
                        Integer.class
                );

        if (sinHomologacion != null
                && sinHomologacion > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + sinHomologacion
                            + " créditos sin homologación "
                            + "individual completa."
            );
        }


        // =====================================================
        // 4. ALINEACIÓN POR DOCUMENTO + MODELO
        //
        // Para cada deudor dentro del mismo modelo PE se toma
        // la peor edad homologada de sus obligaciones.
        //
        // Esa será:
        //
        // edad_contable_pe
        // =====================================================

        jdbcTemplate.update(
                """
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
    
                WHERE m.documento =
                      v.documento
    
                  AND m.id_modelo_pe =
                      v.id_modelo_pe
                """
        );


        // =====================================================
        // 5. VALIDAR ALINEACIÓN FINAL
        // =====================================================

        Integer incompletos =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
    
                        FROM tmp_pe_variables
    
                        WHERE dias_mora_homologacion IS NULL
                           OR edad_homologada_individual IS NULL
                           OR edad_contable_pe IS NULL
                        """,
                        Integer.class
                );

        if (incompletos != null
                && incompletos > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + incompletos
                            + " créditos sin homologación "
                            + "o alineación PE."
            );
        }


        // =====================================================
        // 6. VALIDAR EDADES PERMITIDAS
        // =====================================================

        Integer edadesInvalidas =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
    
                        FROM tmp_pe_variables
    
                        WHERE edad_homologada_individual
                              NOT IN ('A', 'B', 'C', 'D', 'E')
    
                           OR edad_contable_pe
                              NOT IN ('A', 'B', 'C', 'D', 'E')
                        """,
                        Integer.class
                );

        if (edadesInvalidas != null
                && edadesInvalidas > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + edadesInvalidas
                            + " créditos con edades PE "
                            + "de homologación o alineación inválidas."
            );
        }


        // =====================================================
        // 7. RESULTADO
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
