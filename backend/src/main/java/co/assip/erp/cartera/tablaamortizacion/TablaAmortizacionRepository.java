package co.assip.erp.cartera.tablaamortizacion;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class TablaAmortizacionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public TablaAmortizacionRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    // =========================================================
    // SOLICITUD
    // =========================================================

    public Optional<SolicitudTablaDatos> buscarSolicitud(
            Integer idSolicitudCredito
    ) {

        String sql = """
                SELECT
                    v.id_solicitud_credito,
                    v.numero_solicitud,

                    v.id_agencia,

                    v.id_datos_personal,
                    v.documento,
                    v.nombre_completo,

                    v.id_linea_credito,
                    v.nombre_linea_credito,

                    v.valor_solicitado,
                    v.plazo_solicitado,
                    v.amortizacion_capital,

                    v.periodo_codigo_interes,
                    v.tipo_modalidad_interes,
                    v.nombre_modalidad_interes,

                    mi.periodo_meses,

                    v.codigo_tipo_cuota,
                    v.nombre_tipo_cuota,

                    v.tasa_colocacion_aplicada,

                    v.valor_cuota_proyectada

                FROM cartera.vw_solicitudes_creditos v

                INNER JOIN cartera.modalidades_intereses mi
                    ON TRIM(mi.periodo_codigo) =
                       TRIM(v.periodo_codigo_interes)

                   AND TRIM(mi.tipo_modalidad) =
                       TRIM(v.tipo_modalidad_interes)

                   AND mi.activo = true

                WHERE v.id_solicitud_credito =
                      :idSolicitudCredito

                  AND v.activo = true
                """;

        List<SolicitudTablaDatos> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue(
                                        "idSolicitudCredito",
                                        idSolicitudCredito
                                ),
                        (rs, rowNum) ->
                                new SolicitudTablaDatos(
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
                                        rs.getString(
                                                "documento"
                                        ),
                                        rs.getString(
                                                "nombre_completo"
                                        ),
                                        rs.getObject(
                                                "id_linea_credito",
                                                Integer.class
                                        ),
                                        rs.getString(
                                                "nombre_linea_credito"
                                        ),
                                        rs.getBigDecimal(
                                                "valor_solicitado"
                                        ),
                                        rs.getObject(
                                                "plazo_solicitado",
                                                Integer.class
                                        ),
                                        rs.getObject(
                                                "amortizacion_capital",
                                                Integer.class
                                        ),
                                        rs.getString(
                                                "periodo_codigo_interes"
                                        ),
                                        rs.getString(
                                                "tipo_modalidad_interes"
                                        ),
                                        rs.getString(
                                                "nombre_modalidad_interes"
                                        ),
                                        rs.getObject(
                                                "periodo_meses",
                                                Integer.class
                                        ),
                                        rs.getString(
                                                "codigo_tipo_cuota"
                                        ),
                                        rs.getString(
                                                "nombre_tipo_cuota"
                                        ),
                                        rs.getBigDecimal(
                                                "tasa_colocacion_aplicada"
                                        ),
                                        rs.getBigDecimal(
                                                "valor_cuota_proyectada"
                                        )
                                )
                );

        if (resultados.isEmpty()) {
            return Optional.empty();
        }

        if (resultados.size() > 1) {

            throw new IllegalStateException(
                    "Se encontró más de una configuración de intereses para la solicitud "
                            + idSolicitudCredito
                            + "."
            );
        }

        return Optional.of(
                resultados.get(0)
        );
    }


    // =========================================================
    // DATOS INTERNOS
    // =========================================================

    public record SolicitudTablaDatos(

            Integer idSolicitudCredito,
            String numeroSolicitud,

            Integer idAgencia,

            Integer idDatosPersonal,
            String documento,
            String nombreCompleto,

            Integer idLineaCredito,
            String nombreLineaCredito,

            BigDecimal valorSolicitado,

            Integer plazoSolicitado,
            Integer amortizacionCapital,

            String periodoCodigoInteres,
            String tipoModalidadInteres,
            String nombreModalidadInteres,
            Integer periodoMeses,

            String codigoTipoCuota,
            String nombreTipoCuota,

            BigDecimal tasaColocacionAplicada,

            BigDecimal valorCuotaProyectada

    ) {
    }
}