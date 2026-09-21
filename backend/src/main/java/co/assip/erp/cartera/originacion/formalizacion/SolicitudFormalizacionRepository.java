package co.assip.erp.cartera.originacion.formalizacion;

import co.assip.erp.cartera.originacion.formalizacion.dto.SolicitudFormalizacionDetalleDTO;
import co.assip.erp.cartera.originacion.formalizacion.dto.SolicitudFormalizacionGuardarRequestDTO;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class SolicitudFormalizacionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private static final BeanPropertyRowMapper<SolicitudFormalizacionDetalleDTO>
            DETALLE_MAPPER =
            BeanPropertyRowMapper.newInstance(
                    SolicitudFormalizacionDetalleDTO.class
            );

    public SolicitudFormalizacionRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // CONSULTAR DETALLE DE FORMALIZACIÓN
    // =========================================================

    public Optional<SolicitudFormalizacionDetalleDTO> buscarPorId(
            Integer idSolicitudCredito
    ) {

        String sql = """
            SELECT
                -- =====================================================
                -- IDENTIFICACIÓN
                -- =====================================================

                v.id_solicitud_credito,
                v.numero_solicitud,

                v.id_agencia,
                v.nombre_agencia,

                v.id_datos_personal,
                v.tipo_documento,
                v.documento,
                v.nombre_completo,

                -- =====================================================
                -- ESTADO
                -- =====================================================

                v.id_solicitud_proceso,
                v.nombre_proceso,

                v.id_solicitud_resultado,
                v.nombre_resultado,

                -- =====================================================
                -- CONDICIONES SOLICITADAS
                -- =====================================================

                v.id_linea_credito,
                v.nombre_linea_credito,

                v.valor_solicitado,
                v.plazo_solicitado,

                v.codigo_forma_pago,
                v.periodo_codigo_interes,
                v.tipo_modalidad_interes,

                v.amortizacion_capital,
                v.codigo_tipo_cuota,

                v.meses_gracia_capital,
                v.meses_gracia_interes,

                v.tasa_colocacion_aplicada,
                v.tasa_efectiva_anual,
                v.valor_cuota_proyectada,

                -- =====================================================
                -- GARANTÍAS
                -- =====================================================

                v.codigo_garantia_credito,
                v.nombre_garantia_credito,

                v.id_fondo_garantia,
                v.nombre_fondo_garantia,

                -- =====================================================
                -- CONDICIONES FORMALIZADAS
                -- =====================================================

                s.valor_formalizado,
                s.plazo_formalizado,

                s.codigo_forma_pago_formalizada,
                s.periodo_codigo_interes_formalizado,
                s.tipo_modalidad_interes_formalizado,

                s.amortizacion_capital_formalizada,
                s.codigo_tipo_cuota_formalizada,

                s.meses_gracia_capital_formalizados,
                s.meses_gracia_interes_formalizados,

                s.tasa_nominal_formalizada,
                s.tasa_efectiva_anual_formalizada,
                s.valor_cuota_formalizada,

                -- =====================================================
                -- CONTROL
                -- =====================================================

                s.condiciones_modificadas,

                s.fecha_fin_aprobacion,
                s.fecha_fin_formalizacion,

                -- =====================================================
                -- DECISIÓN APROBADA DEL CICLO VIGENTE
                -- =====================================================

                v.id_ente_aprobacion,
                v.fecha_decision,
                v.observaciones_aprobacion,
                v.numero_acta,
                v.fecha_acta

            FROM cartera.solicitudes_creditos s

            INNER JOIN cartera.vw_solicitudes_creditos v
                ON v.id_solicitud_credito =
                   s.id_solicitud_credito

            WHERE s.id_solicitud_credito =
                  :idSolicitudCredito

              AND s.activo = true
            """;

        List<SolicitudFormalizacionDetalleDTO> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idSolicitudCredito",
                                        idSolicitudCredito
                                ),
                        DETALLE_MAPPER
                );

        return resultados.stream().findFirst();
    }

    // =========================================================
    // GUARDAR CONDICIONES DEFINITIVAS
    //
    // Únicamente permite modificar solicitudes que estén
    // en el proceso 4 - FORMALIZACIÓN.
    //
    // Los cálculos financieros se reciben del Service.
    // =========================================================

    public int guardarCondiciones(
            Integer idSolicitudCredito,
            SolicitudFormalizacionGuardarRequestDTO request,
            BigDecimal tasaEfectivaAnualFormalizada,
            BigDecimal valorCuotaFormalizada,
            boolean condicionesModificadas,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cartera.solicitudes_creditos

            SET
                -- =====================================================
                -- CONDICIONES DEFINITIVAS
                -- =====================================================

                valor_formalizado =
                    :valorFormalizado,

                plazo_formalizado =
                    :plazoFormalizado,

                codigo_forma_pago_formalizada =
                    :codigoFormaPagoFormalizada,

                periodo_codigo_interes_formalizado =
                    :periodoCodigoInteresFormalizado,

                tipo_modalidad_interes_formalizado =
                    :tipoModalidadInteresFormalizado,

                amortizacion_capital_formalizada =
                    :amortizacionCapitalFormalizada,

                codigo_tipo_cuota_formalizada =
                    :codigoTipoCuotaFormalizada,

                meses_gracia_capital_formalizados =
                    :mesesGraciaCapitalFormalizados,

                meses_gracia_interes_formalizados =
                    :mesesGraciaInteresFormalizados,

                tasa_nominal_formalizada =
                    :tasaNominalFormalizada,

                tasa_efectiva_anual_formalizada =
                    :tasaEfectivaAnualFormalizada,

                valor_cuota_formalizada =
                    :valorCuotaFormalizada,

                condiciones_modificadas =
                    :condicionesModificadas,

                -- =====================================================
                -- AUDITORÍA
                -- =====================================================

                fecha_ultima_gestion =
                    CURRENT_TIMESTAMP,

                fk_seguridad_edicion =
                    :idUsuario,

                fecha_edicion =
                    CURRENT_TIMESTAMP

            WHERE id_solicitud_credito =
                  :idSolicitudCredito

              AND activo = true

              AND id_solicitud_proceso = 4

              AND id_solicitud_resultado = 1

              AND fecha_fin_formalizacion IS NULL
              AND id_asesor = :idUsuario
              
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()

                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        )

                        .addValue(
                                "valorFormalizado",
                                request.getValorFormalizado()
                        )

                        .addValue(
                                "plazoFormalizado",
                                request.getPlazoFormalizado()
                        )

                        .addValue(
                                "codigoFormaPagoFormalizada",
                                request.getCodigoFormaPagoFormalizada()
                        )

                        .addValue(
                                "periodoCodigoInteresFormalizado",
                                request.getPeriodoCodigoInteresFormalizado()
                        )

                        .addValue(
                                "tipoModalidadInteresFormalizado",
                                request.getTipoModalidadInteresFormalizado()
                        )

                        .addValue(
                                "amortizacionCapitalFormalizada",
                                request.getAmortizacionCapitalFormalizada()
                        )

                        .addValue(
                                "codigoTipoCuotaFormalizada",
                                request.getCodigoTipoCuotaFormalizada()
                        )

                        .addValue(
                                "mesesGraciaCapitalFormalizados",
                                request.getMesesGraciaCapitalFormalizados()
                        )

                        .addValue(
                                "mesesGraciaInteresFormalizados",
                                request.getMesesGraciaInteresFormalizados()
                        )

                        .addValue(
                                "tasaNominalFormalizada",
                                request.getTasaNominalFormalizada()
                        )

                        .addValue(
                                "tasaEfectivaAnualFormalizada",
                                tasaEfectivaAnualFormalizada
                        )

                        .addValue(
                                "valorCuotaFormalizada",
                                valorCuotaFormalizada
                        )

                        .addValue(
                                "condicionesModificadas",
                                condicionesModificadas
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
    // FINALIZAR FORMALIZACIÓN
    //
    // Proceso 4 - FORMALIZACIÓN
    //         ↓
    // Proceso 5 - DESEMBOLSO
    //
    // No constituye todavía el crédito.
    // =========================================================

    public int finalizarFormalizacion(
            Integer idSolicitudCredito,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cartera.solicitudes_creditos

            SET
                id_solicitud_proceso = 5,

                id_solicitud_resultado = 1,

                fecha_fin_formalizacion =
                    CURRENT_TIMESTAMP,

                fecha_ultima_gestion =
                    CURRENT_TIMESTAMP,

                fk_seguridad_edicion =
                    :idUsuario,

                fecha_edicion =
                    CURRENT_TIMESTAMP

            WHERE id_solicitud_credito =
                  :idSolicitudCredito

              AND activo = true

              AND id_solicitud_proceso = 4

              AND id_solicitud_resultado = 1

              AND fecha_fin_formalizacion IS NULL
                
              AND id_asesor = :idUsuario

              -- =====================================================
              -- CONDICIONES DEFINITIVAS COMPLETAS
              -- =====================================================

              AND valor_formalizado IS NOT NULL

              AND plazo_formalizado IS NOT NULL

              AND codigo_forma_pago_formalizada IS NOT NULL

              AND periodo_codigo_interes_formalizado IS NOT NULL

              AND tipo_modalidad_interes_formalizado IS NOT NULL

              AND amortizacion_capital_formalizada IS NOT NULL

              AND codigo_tipo_cuota_formalizada IS NOT NULL

              AND meses_gracia_capital_formalizados IS NOT NULL

              AND meses_gracia_interes_formalizados IS NOT NULL

              AND tasa_nominal_formalizada IS NOT NULL

              AND tasa_efectiva_anual_formalizada IS NOT NULL

              AND valor_cuota_formalizada IS NOT NULL

              AND condiciones_modificadas IS NOT NULL
            """;

        return jdbc.update(
                sql,
                new MapSqlParameterSource()

                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        )

                        .addValue(
                                "idUsuario",
                                idUsuario
                        )
        );
    }

}