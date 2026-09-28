package co.assip.erp.cartera.originacion.impresion;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class OriginacionPagareRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public OriginacionPagareRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    // =========================================================
    // DATOS DEL PAGARÉ
    //
    // Fuente:
    // - Solicitud formalizada.
    // - Crédito ya constituido.
    //
    // No genera pagaré ni modifica condiciones financieras.
    // =========================================================

    private static final String SQL_PAGARE = """
            SELECT
                s.id_solicitud_credito,
                s.numero_solicitud,

                s.id_agencia,
                s.id_datos_personal,
                s.id_cuenta_aportes,
                s.id_linea_credito,

                s.id_solicitud_proceso,
                s.id_solicitud_resultado,

                s.id_cartera_credito,

                cc.pagare_cartera,
                cc.codigo_estado_cartera,

                -- Fechas definitivas del crédito constituido.
                cc.fecha_inclusion_sistema,
                cc.fecha_primera_cuota,
                cc.fecha_primera_cuota_capital,
                cc.fecha_primera_cuota_interes,
                cc.fecha_final,

                -- Condiciones que quedaron constituidas en cartera.
                cc.meses_gracia_capital,
                cc.meses_gracia_interes,
                cc.codigo_garantia_credito,
                gar.descripcion_garantia_credito,
                cc.codigo_destino_economico,
                des.descripcion_destino_economico,

                -- Número visible de la cuenta de aportes vinculada.
                ca.codigo_cuenta AS numero_cuenta_aportes,

                lc.codigo_linea_credito,
                lc.nombre_linea_credito,

                s.valor_formalizado,
                s.plazo_formalizado,
                s.amortizacion_capital_formalizada,
                s.valor_cuota_formalizada,

                s.tasa_nominal_formalizada,
                s.tasa_efectiva_anual_formalizada,

                s.codigo_forma_pago_formalizada,
                s.periodo_codigo_interes_formalizado,
                s.tipo_modalidad_interes_formalizado,
                s.codigo_tipo_cuota_formalizada

            FROM cartera.solicitudes_creditos s

            INNER JOIN cartera.carteras_creditos cc
                ON cc.id_cartera_credito =
                   s.id_cartera_credito

               AND cc.id_agencia =
                   s.id_agencia

               AND cc.id_datos_personal =
                   s.id_datos_personal

            INNER JOIN cartera.lineas_creditos lc
                ON lc.id_linea_credito =
                   s.id_linea_credito

            LEFT JOIN depositos.cuentas_ahorro ca
                ON ca.id_cuenta_ahorro = cc.id_cuenta_aportes

            LEFT JOIN cartera.garantias_creditos gar
                ON gar.codigo_garantia_credito = cc.codigo_garantia_credito

            LEFT JOIN cartera.destinos_economicos des
                ON des.codigo_destino_economico = cc.codigo_destino_economico

            WHERE s.id_solicitud_credito =
                  :idSolicitudCredito

              AND s.activo = true

              AND s.id_cartera_credito IS NOT NULL

              AND cc.pagare_cartera IS NOT NULL

              AND TRIM(cc.pagare_cartera) <> ''
            """;


    // =========================================================
    // FIRMANTES DEL PAGARÉ
    //
    // Incluye:
    // - Deudor principal.
    // - Todos los codeudores activos.
    //
    // No se limita a tres codeudores como el VB.
    //
    // La ubicación se selecciona de forma determinística.
    // Si existen varias ubicaciones, toma la de mayor ID.
    // =========================================================

    private static final String SQL_FIRMANTES = """
            SELECT
                sd.id_solicitud_deudor,
                sd.id_solicitud_credito,
                sd.id_datos_personal,

                sd.tipo_deudor,
                sd.orden_deudor,

                dp.tipo_documento,
                dp.documento,

                NULLIF(
                    TRIM(
                        CONCAT_WS(
                            ' ',
                            NULLIF(TRIM(dp.nombres), ''),
                            NULLIF(TRIM(dp.primer_apellido), ''),
                            NULLIF(TRIM(dp.segundo_apellido), '')
                        )
                    ),
                    ''
                ) AS nombre_completo,

                u.direccion,
                u.telefono,
                u.celular_uno,
                u.celular_dos,
                u.correo

            FROM cartera.solicitudes_deudores sd

            INNER JOIN cartera.solicitudes_creditos s
                ON s.id_solicitud_credito =
                   sd.id_solicitud_credito

               AND s.activo = true

            INNER JOIN hoja_vida.datos_personales dp
                ON dp.id_datos_personal =
                   sd.id_datos_personal

            LEFT JOIN LATERAL (
                SELECT
                    ub.direccion,
                    ub.telefono,
                    ub.celular_uno,
                    ub.celular_dos,
                    ub.correo

                FROM hoja_vida.ubicaciones ub

                WHERE ub.id_datos_personal =
                      sd.id_datos_personal

                ORDER BY
                    ub.id_ubicacion DESC

                LIMIT 1
            ) u ON true

            WHERE sd.id_solicitud_credito =
                  :idSolicitudCredito

              AND sd.activo = true

              AND UPPER(TRIM(sd.tipo_deudor))
                  IN ('PRINCIPAL', 'CODEUDOR')

            ORDER BY
                CASE
                    WHEN UPPER(TRIM(sd.tipo_deudor))
                         = 'PRINCIPAL'
                    THEN 0
                    ELSE 1
                END,

                sd.orden_deudor,
                sd.id_solicitud_deudor
            """;


    // =========================================================
    // CONSULTAR PAGARÉ
    // =========================================================

    public Optional<PagareDatos> buscarPagare(
            Integer idSolicitudCredito
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        );

        List<PagareDatos> resultados =
                jdbc.query(
                        SQL_PAGARE,
                        parametros,
                        (rs, rowNum) ->
                                new PagareDatos(
                                        rs.getInt(
                                                "id_solicitud_credito"
                                        ),
                                        rs.getString(
                                                "numero_solicitud"
                                        ),

                                        rs.getInt(
                                                "id_agencia"
                                        ),
                                        rs.getInt(
                                                "id_datos_personal"
                                        ),
                                        rs.getObject(
                                                "id_cuenta_aportes",
                                                Integer.class
                                        ),
                                        rs.getInt(
                                                "id_linea_credito"
                                        ),

                                        rs.getInt(
                                                "id_solicitud_proceso"
                                        ),
                                        rs.getInt(
                                                "id_solicitud_resultado"
                                        ),

                                        rs.getInt(
                                                "id_cartera_credito"
                                        ),
                                        rs.getString(
                                                "pagare_cartera"
                                        ),
                                        rs.getString(
                                                "codigo_estado_cartera"
                                        ),

                                        rs.getObject(
                                                "fecha_inclusion_sistema",
                                                LocalDate.class
                                        ),
                                        rs.getObject(
                                                "fecha_primera_cuota",
                                                LocalDate.class
                                        ),
                                        rs.getObject(
                                                "fecha_primera_cuota_capital",
                                                LocalDate.class
                                        ),
                                        rs.getObject(
                                                "fecha_primera_cuota_interes",
                                                LocalDate.class
                                        ),
                                        rs.getObject(
                                                "fecha_final",
                                                LocalDate.class
                                        ),

                                        rs.getObject(
                                                "meses_gracia_capital",
                                                Integer.class
                                        ),
                                        rs.getObject(
                                                "meses_gracia_interes",
                                                Integer.class
                                        ),
                                        rs.getString(
                                                "codigo_garantia_credito"
                                        ),
                                        rs.getString(
                                                "descripcion_garantia_credito"
                                        ),
                                        rs.getString(
                                                "codigo_destino_economico"
                                        ),
                                        rs.getString(
                                                "descripcion_destino_economico"
                                        ),
                                        rs.getString(
                                                "numero_cuenta_aportes"
                                        ),

                                        rs.getString(
                                                "codigo_linea_credito"
                                        ),
                                        rs.getString(
                                                "nombre_linea_credito"
                                        ),

                                        rs.getBigDecimal(
                                                "valor_formalizado"
                                        ),
                                        rs.getObject(
                                                "plazo_formalizado",
                                                Integer.class
                                        ),
                                        rs.getObject(
                                                "amortizacion_capital_formalizada",
                                                Integer.class
                                        ),
                                        rs.getBigDecimal(
                                                "valor_cuota_formalizada"
                                        ),

                                        rs.getBigDecimal(
                                                "tasa_nominal_formalizada"
                                        ),
                                        rs.getBigDecimal(
                                                "tasa_efectiva_anual_formalizada"
                                        ),

                                        rs.getString(
                                                "codigo_forma_pago_formalizada"
                                        ),
                                        rs.getString(
                                                "periodo_codigo_interes_formalizado"
                                        ),
                                        rs.getString(
                                                "tipo_modalidad_interes_formalizado"
                                        ),
                                        rs.getString(
                                                "codigo_tipo_cuota_formalizada"
                                        )
                                )
                );

        return resultados.stream().findFirst();
    }


    // =========================================================
    // CONSULTAR FIRMANTES
    // =========================================================

    public List<FirmanteDatos> listarFirmantes(
            Integer idSolicitudCredito
    ) {

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idSolicitudCredito",
                                idSolicitudCredito
                        );

        return jdbc.query(
                SQL_FIRMANTES,
                parametros,
                (rs, rowNum) ->
                        new FirmanteDatos(
                                rs.getInt(
                                        "id_solicitud_deudor"
                                ),
                                rs.getInt(
                                        "id_solicitud_credito"
                                ),
                                rs.getInt(
                                        "id_datos_personal"
                                ),

                                rs.getString(
                                        "tipo_deudor"
                                ),
                                rs.getObject(
                                        "orden_deudor",
                                        Integer.class
                                ),

                                rs.getString(
                                        "tipo_documento"
                                ),
                                rs.getString(
                                        "documento"
                                ),
                                rs.getString(
                                        "nombre_completo"
                                ),

                                rs.getString(
                                        "direccion"
                                ),
                                rs.getString(
                                        "telefono"
                                ),
                                rs.getString(
                                        "celular_uno"
                                ),
                                rs.getString(
                                        "celular_dos"
                                ),
                                rs.getString(
                                        "correo"
                                )
                        )
        );
    }


    // =========================================================
    // DATOS DEL CRÉDITO Y PAGARÉ
    // =========================================================

    public record PagareDatos(

            Integer idSolicitudCredito,
            String numeroSolicitud,

            Integer idAgencia,
            Integer idDatosPersonal,
            Integer idCuentaAportes,
            Integer idLineaCredito,

            Integer idSolicitudProceso,
            Integer idSolicitudResultado,

            Integer idCarteraCredito,
            String pagareCartera,
            String codigoEstadoCartera,

            LocalDate fechaInclusionSistema,
            LocalDate fechaPrimeraCuota,
            LocalDate fechaPrimeraCuotaCapital,
            LocalDate fechaPrimeraCuotaInteres,
            LocalDate fechaFinal,

            Integer mesesGraciaCapital,
            Integer mesesGraciaInteres,
            String codigoGarantiaCredito,
            String descripcionGarantiaCredito,
            String codigoDestinoEconomico,
            String descripcionDestinoEconomico,
            String numeroCuentaAportes,

            String codigoLineaCredito,
            String nombreLineaCredito,

            BigDecimal valorFormalizado,
            Integer plazoFormalizado,
            Integer amortizacionCapitalFormalizada,
            BigDecimal valorCuotaFormalizada,

            BigDecimal tasaNominalFormalizada,
            BigDecimal tasaEfectivaAnualFormalizada,

            String codigoFormaPagoFormalizada,
            String periodoCodigoInteresFormalizado,
            String tipoModalidadInteresFormalizado,
            String codigoTipoCuotaFormalizada

    ) {
    }


    // =========================================================
    // DATOS DEL FIRMANTE
    // =========================================================

    public record FirmanteDatos(

            Integer idSolicitudDeudor,
            Integer idSolicitudCredito,
            Integer idDatosPersonal,

            String tipoDeudor,
            Integer ordenDeudor,

            String tipoDocumento,
            String documento,
            String nombreCompleto,

            String direccion,
            String telefono,
            String celularUno,
            String celularDos,
            String correo

    ) {

        public String telefonoContacto() {

            if (telefono != null
                    && !telefono.isBlank()) {
                return telefono.trim();
            }

            if (celularUno != null
                    && !celularUno.isBlank()) {
                return celularUno.trim();
            }

            if (celularDos != null
                    && !celularDos.isBlank()) {
                return celularDos.trim();
            }

            return "";
        }
    }
}
