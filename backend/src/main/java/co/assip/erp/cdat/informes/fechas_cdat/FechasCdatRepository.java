package co.assip.erp.cdat.informes.fechas_cdat;

import co.assip.erp.cdat.informes.fechas_cdat.dto.FechasCdatItemDTO;
import co.assip.erp.cdat.informes.fechas_cdat.dto.FechasCdatRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FechasCdatRepository {

    private final JdbcTemplate jdbc;

    public List<FechasCdatItemDTO> consultar(
            FechasCdatRequestDTO request
    ) {

        String condicion = obtenerCondicion(
                request.getTipoInforme()
        );

        String sql = """
                SELECT
                    v.id_cuenta_cdat,
                    v.codigo_cdat,

                    COALESCE(
                        v.documento,
                        ''
                    ) AS documento,

                    COALESCE(
                        v.nombre_completo_apellidos,
                        v.nombre_completo_nombres,
                        ''
                    ) AS nombre_completo,

                    COALESCE(
                        v.nombre_agencia,
                        ''
                    ) AS agencia,

                    v.fecha_apertura_cdat,
                    v.fecha_vencimiento_cdat,

                    v.plazo_meses,

                    v.tasa_nominal_anual,

                    v.saldo_actual_cdat,

                    COALESCE(
                        v.descripcion_estado_cdat,
                        ''
                    ) AS estado_cdat

                FROM cdat.vw_cdat_cuentas_total_extendida v

                WHERE 1 = 1
                """ + condicion + """

                ORDER BY
                    v.fecha_apertura_cdat DESC,
                    v.codigo_cdat DESC
                """;

        return jdbc.query(
                sql,
                (rs, rowNum) ->
                        FechasCdatItemDTO.builder()
                                .idCuentaCdat(
                                        rs.getLong("id_cuenta_cdat")
                                )
                                .codigoCdat(
                                        rs.getString("codigo_cdat")
                                )
                                .documento(
                                        rs.getString("documento")
                                )
                                .nombreCompleto(
                                        rs.getString("nombre_completo")
                                )
                                .agencia(
                                        rs.getString("agencia")
                                )
                                .fechaApertura(
                                        rs.getDate("fecha_apertura_cdat") == null
                                                ? null
                                                : rs.getDate("fecha_apertura_cdat").toLocalDate()
                                )
                                .fechaVencimiento(
                                        rs.getDate("fecha_vencimiento_cdat") == null
                                                ? null
                                                : rs.getDate("fecha_vencimiento_cdat").toLocalDate()
                                )
                                .plazoMeses(
                                        rs.getInt("plazo_meses")
                                )
                                .tasa(
                                        rs.getBigDecimal("tasa_nominal_anual")
                                )
                                .valor(
                                        rs.getBigDecimal("saldo_actual_cdat")
                                )
                                .estado(
                                        rs.getString("estado_cdat")
                                )
                                .build(),
                request.getFechaInicial(),
                request.getFechaFinal()
        );
    }

    private String obtenerCondicion(
            String tipoInforme
    ) {

        if (tipoInforme == null) {
            throw new RuntimeException(
                    "Tipo informe requerido."
            );
        }

        return switch (tipoInforme) {

            case "NUEVOS" -> """
                    
                    AND v.fecha_apertura_cdat
                        BETWEEN ? AND ?
                    """;

            case "CANCELADOS" -> """
                    
                    AND v.fecha_cancelacion_cdat IS NOT NULL
                    
                    AND v.fecha_cancelacion_cdat
                        BETWEEN ? AND ?
                    """;

            case "VENCER" -> """
                    
                    AND v.fecha_vencimiento_cdat
                        BETWEEN ? AND ?
                    
                    AND UPPER(v.descripcion_estado_cdat)
                        = 'ACTIVO'
                    """;

            case "VENCIDOS" -> """
                    
                    AND v.fecha_vencimiento_cdat
                        BETWEEN ? AND ?
                    
                    AND UPPER(v.descripcion_estado_cdat)
                        <> 'ACTIVO'
                    """;

            case "RENOVADOS" -> """
                    
                    AND v.fecha_renovacion_cdat IS NOT NULL
                    
                    AND v.fecha_renovacion_cdat
                        BETWEEN ? AND ?
                    """;

            default -> throw new RuntimeException(
                    "Tipo informe no válido: "
                            + tipoInforme
            );
        };
    }

}