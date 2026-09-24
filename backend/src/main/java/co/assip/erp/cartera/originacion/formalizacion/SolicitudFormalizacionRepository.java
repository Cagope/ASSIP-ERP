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
                v.fecha_acta,

                -- =====================================================
                -- PAGARÉ Y CRÉDITO CONSTITUIDO
                -- =====================================================

                s.id_cartera_credito,

                cc.pagare_cartera,
                cc.codigo_estado_cartera

            FROM cartera.solicitudes_creditos s

            INNER JOIN cartera.vw_solicitudes_creditos v
                ON v.id_solicitud_credito =
                   s.id_solicitud_credito
                   
            LEFT JOIN cartera.carteras_creditos cc
                ON cc.id_cartera_credito =
                   s.id_cartera_credito

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
              AND id_cartera_credito IS NULL
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
    // El crédito ya fue constituido en estado P.
    // Este método únicamente traslada la solicitud
    // de Formalización a Desembolso.
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
                
              AND id_cartera_credito IS NOT NULL

              AND EXISTS (
                  SELECT 1
                  FROM cartera.carteras_creditos cc
                  WHERE cc.id_cartera_credito =
                        cartera.solicitudes_creditos.id_cartera_credito

                    AND cc.id_agencia =
                        cartera.solicitudes_creditos.id_agencia

                    AND cc.id_datos_personal =
                        cartera.solicitudes_creditos.id_datos_personal

                    AND cc.codigo_estado_cartera = 'P'

                    AND cc.pagare_cartera IS NOT NULL

                    AND TRIM(cc.pagare_cartera) <> ''
              )
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

    // =========================================================
    // GENERACIÓN DE PAGARÉ
    // BLOQUEAR SOLICITUD
    // =========================================================

    public Optional<ContextoGeneracionPagare> bloquearParaGenerarPagare(
            Integer idSolicitudCredito
    ) {

        String sql = """
            SELECT
                s.id_solicitud_credito,
                s.id_agencia,
                s.id_datos_personal,
                s.id_linea_credito,
                s.id_cartera_credito,
                s.id_solicitud_proceso,
                s.id_solicitud_resultado,
                s.id_asesor,
                s.fecha_fin_formalizacion

            FROM cartera.solicitudes_creditos s

            WHERE s.id_solicitud_credito = :idSolicitudCredito
              AND s.activo = true

            FOR UPDATE OF s
            """;

        List<ContextoGeneracionPagare> resultados = jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        ),
                (rs, rowNum) -> new ContextoGeneracionPagare(
                        rs.getInt("id_solicitud_credito"),
                        rs.getInt("id_agencia"),
                        rs.getInt("id_datos_personal"),
                        rs.getObject(
                                "id_linea_credito",
                                Integer.class
                        ),
                        rs.getObject(
                                "id_cartera_credito",
                                Integer.class
                        ),
                        rs.getInt("id_solicitud_proceso"),
                        rs.getInt("id_solicitud_resultado"),
                        rs.getObject(
                                "id_asesor",
                                Integer.class
                        ),
                        rs.getTimestamp(
                                "fecha_fin_formalizacion"
                        ) != null
                )
        );

        if (resultados.size() > 1) {
            throw new IllegalStateException(
                    "Se encontró más de una solicitud con identificador "
                            + idSolicitudCredito
            );
        }

        return resultados.stream().findFirst();
    }


    // =========================================================
    // CONSECUTIVO PAGARÉ - PARÁMETRO 605
    //
    // UPDATE sobre la fila del parámetro:
    // PostgreSQL controla la concurrencia por agencia.
    // =========================================================

    public Long siguienteConsecutivoPagare(
            Integer idAgencia,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE general.parametros AS p

            SET
                valor_parametro =
                    COALESCE(p.valor_parametro, 0) + 1,

                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP

            WHERE p.id_agencia = :idAgencia
              AND p.codigo_parametro = 605

            RETURNING p.valor_parametro
            """;

        List<BigDecimal> resultados = jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idAgencia", idAgencia)
                        .addValue("idUsuario", idUsuario),
                (rs, rowNum) ->
                        rs.getBigDecimal("valor_parametro")
        );

        if (resultados.size() != 1
                || resultados.get(0) == null) {

            throw new IllegalStateException(
                    "No se pudo obtener el consecutivo del pagaré. "
                            + "Revise el parámetro 605 de la agencia "
                            + idAgencia
                            + "."
            );
        }

        try {
            return resultados.get(0).longValueExact();

        } catch (ArithmeticException ex) {

            throw new IllegalStateException(
                    "El parámetro 605 de la agencia "
                            + idAgencia
                            + " no contiene un consecutivo entero válido.",
                    ex
            );
        }
    }


    // =========================================================
    // VERIFICAR PAGARÉ EXISTENTE EN TODA LA AGENCIA
    // =========================================================

    public boolean existePagareEnAgencia(
            Integer idAgencia,
            String pagareCartera
    ) {

        String sql = """
            SELECT EXISTS (
                SELECT 1

                FROM cartera.carteras_creditos cc

                WHERE cc.id_agencia = :idAgencia
                  AND TRIM(cc.pagare_cartera) =
                      TRIM(:pagareCartera)
            )
            """;

        Boolean existe = jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("idAgencia", idAgencia)
                        .addValue("pagareCartera", pagareCartera),
                Boolean.class
        );

        return Boolean.TRUE.equals(existe);
    }


    // =========================================================
    // VINCULAR CRÉDITO GENERADO CON SOLICITUD
    // =========================================================

    public int vincularCreditoGenerado(
            Integer idSolicitudCredito,
            Integer idCarteraCredito,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cartera.solicitudes_creditos

            SET
                id_cartera_credito = :idCarteraCredito,

                fecha_ultima_gestion = CURRENT_TIMESTAMP,

                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP

            WHERE id_solicitud_credito = :idSolicitudCredito

              AND activo = true
              AND id_solicitud_proceso = 4
              AND id_solicitud_resultado = 1

              AND fecha_fin_formalizacion IS NULL
              AND id_cartera_credito IS NULL

              AND id_asesor = :idUsuario
            """;

        return jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        )
                        .addValue(
                                "idCarteraCredito",
                                idCarteraCredito
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        )
        );
    }


    // =========================================================
    // CONTEXTO DE GENERACIÓN
    // =========================================================

    public record ContextoGeneracionPagare(
            Integer idSolicitudCredito,
            Integer idAgencia,
            Integer idDatosPersonal,
            Integer idLineaCredito,
            Integer idCarteraCredito,
            Integer idSolicitudProceso,
            Integer idSolicitudResultado,
            Integer idAsesor,
            boolean formalizacionFinalizada
    ) {
    }

    // =========================================================
// GENERAR PAGARÉ - CONSTITUIR CRÉDITO PENDIENTE
//
// Se ejecuta dentro de la transacción del Service.
//
// El crédito se crea en estado P.
// No se registra desembolso ni saldo de cartera.
// =========================================================

    public Integer insertarCreditoDesdeFormalizacion(
            Integer idSolicitudCredito,
            String pagareCartera,
            Integer idUsuario
    ) {

        String sql = """
        INSERT INTO cartera.carteras_creditos
        (
            id_agencia,
            id_linea_credito,
            pagare_cartera,
            id_datos_personal,
            id_cuenta_aportes,

            codigo_clasificacion_credito,
            codigo_garantia_credito,
            codigo_subgarantia,
            codigo_destino_economico,

            periodo_codigo_interes,
            tipo_modalidad_interes,
            amortizacion_capital,
            codigo_tipo_cuota,
            plazo,

            meses_gracia_capital,
            meses_gracia_interes,
            codigo_forma_pago,

            id_empresa_libranza,
            id_ente_aprobacion,

            codigo_estado_cartera,

            valor_inicial_credito,
            valor_desembolsado,
            valor_base_calculo_cuota,
            valor_primera_cuota,
            valor_cuota,
            saldo_actual,

            tasa_nominal_anual,
            tasa_efectiva_anual,

            fecha_inclusion_sistema,
            fecha_contable,

            fk_seguridad_creacion,
            fecha_creacion,
            fk_seguridad_edicion,
            fecha_edicion
        )

        SELECT
            s.id_agencia,
            s.id_linea_credito,
            :pagareCartera,
            s.id_datos_personal,
            s.id_cuenta_aportes,

            s.codigo_clasificacion_credito,
            s.codigo_garantia_credito,
            s.codigo_subgarantia,
            s.codigo_destino_economico,

            s.periodo_codigo_interes_formalizado,
            s.tipo_modalidad_interes_formalizado,
            s.amortizacion_capital_formalizada,
            s.codigo_tipo_cuota_formalizada,
            s.plazo_formalizado,

            s.meses_gracia_capital_formalizados,
            s.meses_gracia_interes_formalizados,
            s.codigo_forma_pago_formalizada,

            s.id_empresa_libranza,
            s.id_ente_aprobacion,

            'P',

            s.valor_formalizado,
            0,
            s.valor_formalizado,
            s.valor_cuota_formalizada,
            s.valor_cuota_formalizada,
            0,

            s.tasa_nominal_formalizada,
            s.tasa_efectiva_anual_formalizada,

            CURRENT_DATE,
            CURRENT_DATE,

            :idUsuario,
            CURRENT_TIMESTAMP,
            :idUsuario,
            CURRENT_TIMESTAMP

        FROM cartera.solicitudes_creditos s

        WHERE s.id_solicitud_credito = :idSolicitudCredito

          AND s.activo = true

          AND s.id_solicitud_proceso = 4
          AND s.id_solicitud_resultado = 1

          AND s.fecha_fin_formalizacion IS NULL

          AND s.id_cartera_credito IS NULL

          AND s.id_asesor = :idUsuario

          AND s.id_agencia IS NOT NULL
          AND s.id_linea_credito IS NOT NULL
          AND s.id_datos_personal IS NOT NULL

          AND s.codigo_clasificacion_credito IS NOT NULL
          AND s.codigo_garantia_credito IS NOT NULL

          AND s.valor_formalizado > 0
          AND s.plazo_formalizado > 0

          AND s.codigo_forma_pago_formalizada IS NOT NULL
          AND s.periodo_codigo_interes_formalizado IS NOT NULL
          AND s.tipo_modalidad_interes_formalizado IS NOT NULL

          AND s.amortizacion_capital_formalizada > 0
          AND s.codigo_tipo_cuota_formalizada IS NOT NULL

          AND s.meses_gracia_capital_formalizados IS NOT NULL
          AND s.meses_gracia_interes_formalizados IS NOT NULL

          AND s.tasa_nominal_formalizada IS NOT NULL
          AND s.tasa_efectiva_anual_formalizada IS NOT NULL

          AND s.valor_cuota_formalizada IS NOT NULL

          AND s.condiciones_modificadas IS NOT NULL

        RETURNING id_cartera_credito
        """;

        List<Integer> resultados = jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        )
                        .addValue(
                                "pagareCartera",
                                pagareCartera
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        ),
                (rs, rowNum) ->
                        rs.getInt("id_cartera_credito")
        );

        if (resultados.size() != 1) {

            throw new IllegalStateException(
                    "No fue posible constituir el crédito de la solicitud "
                            + idSolicitudCredito
                            + ". Verifique las condiciones definitivas, "
                            + "el estado de Formalización y el asesor responsable."
            );
        }

        return resultados.get(0);
    }

}