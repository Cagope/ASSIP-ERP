package co.assip.erp.cartera.cierremensual.anexo2;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class Anexo2ResultadosRepository {

    private final JdbcTemplate jdbcTemplate;

    // Persistencia de trazabilidad PE y resultados oficiales del cierre.

    public int persistirResultadosPe(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        // =====================================================
        // 1. OBTENER POBLACIÓN PE
        // =====================================================

        Integer poblacion =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM tmp_pe_variables
                        """,
                        Integer.class
                );

        int cantidadPoblacion =
                poblacion != null
                        ? poblacion
                        : 0;

        if (cantidadPoblacion <= 0) {

            throw new IllegalStateException(
                    "No existen créditos PE para persistir "
                            + "en el cierre "
                            + idCierreCartera
                            + "."
            );
        }


        // =====================================================
        // 2. VALIDAR QUE TODOS LOS CRÉDITOS PE TENGAN
        //    RESULTADO GENERAL DEL CIERRE
        // =====================================================

        Integer sinResultadoGeneral =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
    
                        FROM tmp_pe_variables v
    
                        LEFT JOIN cartera.cierres_cartera_resultados r
                            ON r.id_cierre_cartera_credito =
                               v.id_cierre_cartera_credito
    
                           AND r.id_cierre_cartera =
                               ?
    
                        WHERE r.id_cierre_cartera_resultado IS NULL
                        """,
                        Integer.class,
                        idCierreCartera
                );

        if (sinResultadoGeneral != null
                && sinResultadoGeneral > 0) {

            throw new IllegalStateException(
                    "Existen "
                            + sinResultadoGeneral
                            + " créditos PE sin registro base en "
                            + "cartera.cierres_cartera_resultados."
            );
        }


        // =====================================================
        // 3. LIMPIAR TRAZABILIDAD PE ANTERIOR DEL CIERRE
        //
        // IMPORTANTE:
        //
        // No se elimina:
        //
        // - fotografía
        // - cierres_cartera_resultados
        // - pe_procesos
        //
        // Solamente la trazabilidad PE regenerable.
        // =====================================================

        jdbcTemplate.update(
                """
                DELETE FROM cartera.pe_resultados
                WHERE id_cierre_cartera = ?
                """,
                idCierreCartera
        );


        // =====================================================
        // 4. INSERTAR TRAZABILIDAD COMPLETA
        //    EN cartera.pe_resultados
        // =====================================================

        String sqlInsertPe = """
        INSERT INTO cartera.pe_resultados
        (
            -- =================================================
            -- IDENTIFICACIÓN
            -- =================================================

            id_cierre_cartera,
            id_cierre_cartera_credito,
            id_cartera_credito,
            id_modelo_pe,
            nombre_modelo_pe,

            -- =================================================
            -- DATOS DE ENTRADA
            -- =================================================

            codigo_clasificacion_credito,
            codigo_forma_pago,
            id_empresa_libranza,
            tipo_persona,
            codigo_garantia_credito,
            fecha_desembolso,

            dias_mora_actual,
            edad_mora_entrada_pe,
            edad_riesgo_entrada_pe,

            saldo_actual,
            saldo_intereses_causados,
            saldo_aportes_fecha_corte,
            valor_aportes_credito,
            valor_costas_judiciales,
            valor_otros_conceptos,

            -- =================================================
            -- RESUMEN HISTÓRICO
            -- =================================================

            mora_max_3m,
            mora_max_12m,
            mora_max_24m,
            mora_max_36m,
            cantidad_mora_31_60_3m,

            -- =================================================
            -- VARIABLES / CONTENIDOS
            -- =================================================

            ea,
            ea_contenido,

            fe,
            fe_contenido,

            valcuota,
            valcuota_contenido,

            fondplazo,
            fondplazo_contenido,

            mora1230,
            mora1230_contenido,

            mora1260,
            mora1260_contenido,

            sinmora,
            sinmora_contenido,

            mora2430n,
            mora2430n_contenido,

            mora315,
            mora315_contenido,

            mortrim,
            mortrim_contenido,

            mora3660,
            mora3660_mora_max_36m,
            mora3660_mora_max_24m,

            -- =================================================
            -- BETAS
            -- =================================================

            beta_intercepto,
            beta_ea,
            beta_fe,
            beta_valcuota,
            beta_fondplazo,
            beta_mora1230,
            beta_mora1260,
            beta_sinmora,
            beta_mora2430n,
            beta_mora315,
            beta_mortrim,
            beta_mora3660,

            -- =================================================
            -- APORTES A Z
            -- =================================================

            aporte_z_intercepto,
            aporte_z_ea,
            aporte_z_fe,
            aporte_z_valcuota,
            aporte_z_fondplazo,
            aporte_z_mora1230,
            aporte_z_mora1260,
            aporte_z_sinmora,
            aporte_z_mora2430n,
            aporte_z_mora315,
            aporte_z_mortrim,
            aporte_z_mora3660,

            -- =================================================
            -- RESULTADO ESTADÍSTICO
            -- =================================================

            z,
            puntaje,
            calificacion_modelo,

            -- =================================================
            -- DEFAULT / PI
            -- =================================================

            dias_default_modelo,
            default_pe,
            calificacion_pe,
            edad_deterioro,
            tipo_entidad_pe,
            calificacion_base_pi,
            pi,

            -- =================================================
            -- VEA
            -- =================================================

            base_vea_capital,
            base_vea_intereses,
            base_vea_costas_judiciales,
            base_vea_otros,
            base_vea_aportes,
            base_vea_ahorro_permanente,

            vea_bruto,
            vea_deducciones,
            vea,

            -- =================================================
            -- GARANTÍA / PDI
            -- =================================================

            codigo_garantia_pdi,
            nombre_garantia_pdi,

            valor_garantia,
            porcentaje_garantia_reconocido,
            valor_garantia_reconocido,

            dias_mora_pdi,
            tramo_pdi,
            dias_desde_pdi,
            dias_hasta_pdi,
            pdi,

            -- =================================================
            -- PÉRDIDA ESPERADA
            -- =================================================

            perdida_esperada,
            porcentaje_perdida,

            -- =================================================
            -- BASES DE DETERIORO
            -- =================================================

            base_deterioro_capital,
            base_deterioro_intereses,
            base_deterioro_otros,
            base_deterioro_total,

            -- =================================================
            -- PORCENTAJES DE DISTRIBUCIÓN
            -- =================================================

            porcentaje_deterioro_capital,
            porcentaje_deterioro_intereses,
            porcentaje_deterioro_otros,

            -- =================================================
            -- PÉRDIDA DISTRIBUIDA
            -- =================================================

            valor_perdida_capital,
            valor_perdida_intereses,
            valor_perdida_otros,

            -- =================================================
            -- DETERIOROS PE
            -- =================================================

            deterioro_capital_pe,
            deterioro_intereses_pe,
            deterioro_otros_pe,
            deterioro_total_pe,

            -- =================================================
            -- HOMOLOGACIÓN / ALINEACIÓN
            -- =================================================

            dias_mora_homologacion,
            edad_homologada_individual,
            edad_contable_pe,

            -- =================================================
            -- AUDITORÍA
            -- =================================================

            fecha_calculo,
            fk_seguridad_creacion,
            fecha_creacion,
            fk_seguridad_edicion,
            fecha_edicion
        )

        SELECT
            -- =================================================
            -- IDENTIFICACIÓN
            -- =================================================

            ? AS id_cierre_cartera,

            v.id_cierre_cartera_credito,
            v.id_cartera_credito,
            v.id_modelo_pe,

            CASE v.id_modelo_pe
                WHEN 1 THEN 'CONSUMO CON LIBRANZA'
                WHEN 2 THEN 'CONSUMO SIN LIBRANZA'
                WHEN 3 THEN 'COMERCIAL PERSONA NATURAL'
                ELSE 'MODELO PE'
            END AS nombre_modelo_pe,

            -- =================================================
            -- DATOS DE ENTRADA
            -- =================================================

            v.codigo_clasificacion_credito,
            v.codigo_forma_pago,
            v.id_empresa_libranza,
            v.tipo_persona,
            v.codigo_garantia_credito,
            v.fecha_desembolso,

            v.dias_mora_actual,
            v.edad_de_mora,
            v.edad_de_riesgo,

            COALESCE(v.saldo_actual, 0),
            COALESCE(v.saldo_intereses_causados, 0),
            COALESCE(v.saldo_aportes_fecha_corte, 0),
            COALESCE(v.valor_aportes_credito, 0),
            COALESCE(v.valor_costas_judiciales, 0),
            COALESCE(v.valor_otros_conceptos, 0),

            -- =================================================
            -- RESUMEN HISTÓRICO
            -- =================================================

            COALESCE(v.mora_max_3m, 0),
            COALESCE(v.mora_max_12m, 0),
            COALESCE(v.mora_max_24m, 0),
            COALESCE(v.mora_max_36m, 0),
            COALESCE(v.cantidad_mora_31_60_3m, 0),

            -- =================================================
            -- VARIABLES / CONTENIDOS
            -- =================================================

            v.ea,
            v.ea_contenido,

            v.fe,
            v.fe_contenido,

            v.valcuota,
            v.valcuota_contenido,

            v.fondplazo,
            v.fondplazo_contenido,

            v.mora1230,
            v.mora1230_contenido,

            v.mora1260,
            v.mora1260_contenido,

            v.sinmora,
            v.sinmora_contenido,

            v.mora2430n,
            v.mora2430n_contenido,

            v.mora315,
            v.mora315_contenido,

            v.mortrim,
            v.mortrim_contenido,

            v.mora3660,
            v.mora3660_mora_max_36m,
            v.mora3660_mora_max_24m,

            -- =================================================
            -- BETAS
            -- =================================================

            v.beta_intercepto,
            v.beta_ea,
            v.beta_fe,
            v.beta_valcuota,
            v.beta_fondplazo,
            v.beta_mora1230,
            v.beta_mora1260,
            v.beta_sinmora,
            v.beta_mora2430n,
            v.beta_mora315,
            v.beta_mortrim,
            v.beta_mora3660,

            -- =================================================
            -- APORTES A Z
            -- =================================================

            v.aporte_z_intercepto,
            v.aporte_z_ea,
            v.aporte_z_fe,
            v.aporte_z_valcuota,
            v.aporte_z_fondplazo,
            v.aporte_z_mora1230,
            v.aporte_z_mora1260,
            v.aporte_z_sinmora,
            v.aporte_z_mora2430n,
            v.aporte_z_mora315,
            v.aporte_z_mortrim,
            v.aporte_z_mora3660,

            -- =================================================
            -- RESULTADO ESTADÍSTICO
            -- =================================================

            v.z,
            v.puntaje,
            v.calificacion_modelo,

            -- =================================================
            -- DEFAULT / PI
            -- =================================================

            v.dias_default_modelo,
            v.default_pe,
            v.calificacion_pe,
            v.edad_deterioro,
            v.tipo_entidad_pe,
            v.calificacion_base_pi,
            v.pi,

            -- =================================================
            -- VEA
            -- =================================================

            COALESCE(v.base_vea_capital, 0),
            COALESCE(v.base_vea_intereses, 0),
            COALESCE(v.base_vea_costas_judiciales, 0),
            COALESCE(v.base_vea_otros, 0),
            COALESCE(v.base_vea_aportes, 0),
            COALESCE(v.base_vea_ahorro_permanente, 0),

            COALESCE(v.vea_bruto, 0),
            COALESCE(v.vea_deducciones, 0),
            COALESCE(v.vea, 0),

            -- =================================================
            -- GARANTÍA / PDI
            -- =================================================

            v.codigo_garantia_pdi,
            v.nombre_garantia_pdi,

            COALESCE(v.valor_garantia, 0),
            COALESCE(v.porcentaje_garantia_reconocido, 0),
            COALESCE(v.valor_garantia_reconocido, 0),

            COALESCE(v.dias_mora_pdi, 0),
            v.tramo_pdi,
            v.dias_desde_pdi,
            v.dias_hasta_pdi,
            COALESCE(v.pdi, 0),

            -- =================================================
            -- PÉRDIDA ESPERADA
            -- =================================================

            COALESCE(v.perdida_valor, 0),
            COALESCE(v.perdida_porce, 0),

            -- =================================================
            -- BASES DE DETERIORO
            -- =================================================

            COALESCE(v.base_deterioro_capital, 0),
            COALESCE(v.base_deterioro_intereses, 0),
            COALESCE(v.base_deterioro_otros, 0),
            COALESCE(v.base_deterioro_total, 0),

            -- =================================================
            -- PORCENTAJES DE DISTRIBUCIÓN
            -- =================================================

            COALESCE(v.porcentaje_deterioro_capital, 0),
            COALESCE(v.porcentaje_deterioro_intereses, 0),
            COALESCE(v.porcentaje_deterioro_otros, 0),

            -- =================================================
            -- PÉRDIDA DISTRIBUIDA
            -- =================================================

            COALESCE(v.valor_perdida_capital, 0),
            COALESCE(v.valor_perdida_intereses, 0),
            COALESCE(v.valor_perdida_otros, 0),

            -- =================================================
            -- DETERIOROS PE
            -- =================================================

            COALESCE(v.deterioro_capital_pe, 0),
            COALESCE(v.deterioro_intereses_pe, 0),
            COALESCE(v.deterioro_otros_pe, 0),
            COALESCE(v.deterioro_total_pe, 0),

            -- =================================================
            -- HOMOLOGACIÓN
            -- =================================================

            COALESCE(v.dias_mora_homologacion, 0),
            v.edad_homologada_individual,
            v.edad_contable_pe,

            -- =================================================
            -- AUDITORÍA
            -- =================================================

            CURRENT_TIMESTAMP,
            ?,
            CURRENT_TIMESTAMP,
            ?,
            CURRENT_TIMESTAMP

        FROM tmp_pe_variables v
        """;

        int resultadosPeInsertados =
                jdbcTemplate.update(
                        sqlInsertPe,
                        idCierreCartera,
                        idUsuario,
                        idUsuario
                );


        // =====================================================
        // 5. VALIDAR CANTIDAD INSERTADA EN pe_resultados
        // =====================================================

        if (resultadosPeInsertados
                != cantidadPoblacion) {

            throw new IllegalStateException(
                    "Inconsistencia al persistir cartera.pe_resultados. "
                            + "Población PE: "
                            + cantidadPoblacion
                            + ". Registros insertados: "
                            + resultadosPeInsertados
                            + "."
            );
        }


        // =====================================================
        // 6. VALIDAR RESULTADOS PE DEL CIERRE
        // =====================================================

        Integer cantidadPersistida =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
    
                        FROM cartera.pe_resultados
    
                        WHERE id_cierre_cartera = ?
                        """,
                        Integer.class,
                        idCierreCartera
                );

        if (cantidadPersistida == null
                || cantidadPersistida
                != cantidadPoblacion) {

            throw new IllegalStateException(
                    "La cantidad persistida en cartera.pe_resultados "
                            + "no coincide con la población PE. "
                            + "Esperados: "
                            + cantidadPoblacion
                            + ". Encontrados: "
                            + (
                            cantidadPersistida != null
                                    ? cantidadPersistida
                                    : 0
                    )
                            + "."
            );
        }


        // =====================================================
        // 7. ACTUALIZAR RESULTADO OFICIAL DEL CIERRE
        //
        // Anexo 1 ya calculó previamente todos los créditos.
        //
        // Aquí solamente sobrescribimos la población PE.
        // =====================================================

        String sqlResultadosCierre = """
        UPDATE cartera.cierres_cartera_resultados r

        SET
            codigo_metodo_calculo =
                'PE',

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

        int resultadosCierreActualizados =
                jdbcTemplate.update(
                        sqlResultadosCierre,
                        idUsuario,
                        idCierreCartera
                );


        // =====================================================
        // 8. VALIDAR CANTIDAD ACTUALIZADA EN RESULTADOS
        // =====================================================

        if (resultadosCierreActualizados
                != cantidadPoblacion) {

            throw new IllegalStateException(
                    "Inconsistencia al actualizar los resultados "
                            + "oficiales PE del cierre. "
                            + "Población PE: "
                            + cantidadPoblacion
                            + ". Resultados actualizados: "
                            + resultadosCierreActualizados
                            + "."
            );
        }


        // =====================================================
        // 9. VALIDAR MÉTODO DE CÁLCULO PE
        // =====================================================

        Integer cantidadMarcadaPe =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
    
                        FROM cartera.cierres_cartera_resultados
    
                        WHERE id_cierre_cartera = ?
    
                          AND codigo_metodo_calculo =
                              'PE'
                        """,
                        Integer.class,
                        idCierreCartera
                );

        if (cantidadMarcadaPe == null
                || cantidadMarcadaPe
                != cantidadPoblacion) {

            throw new IllegalStateException(
                    "La cantidad de resultados marcados como PE "
                            + "no coincide con la población calculada. "
                            + "Esperados: "
                            + cantidadPoblacion
                            + ". Encontrados: "
                            + (
                            cantidadMarcadaPe != null
                                    ? cantidadMarcadaPe
                                    : 0
                    )
                            + "."
            );
        }


        // =====================================================
        // 10. RESULTADO
        //
        // Se devuelve la cantidad de créditos PE persistidos.
        // =====================================================

        return resultadosCierreActualizados;
    }


}
