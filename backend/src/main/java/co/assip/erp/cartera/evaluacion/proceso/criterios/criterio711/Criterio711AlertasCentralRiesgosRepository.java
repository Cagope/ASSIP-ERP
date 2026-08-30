package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio711;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio711.dto.Criterio711DatoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class Criterio711AlertasCentralRiesgosRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // OBTENER DATOS DEL CRITERIO 711
    //
    // Para cada crÃ©dito se buscan las alertas de la central
    // utilizando la combinaciÃ³n exacta:
    //
    // fecha de corte
    // + documento
    // + clasificaciÃ³n del crÃ©dito
    //
    // Si existen varias cargas para la misma fecha de corte,
    // se utiliza el archivo importado mÃ¡s recientemente.
    // =========================================================

    public List<Criterio711DatoDTO> obtenerDatos(
            LocalDate fechaCorte
    ) {

        String sql = """
                WITH archivo_corte AS
                (
                    SELECT
                        a.id_central_archivo,
                        a.nombre_archivo

                    FROM cartera.evaluaciones_cartera_central_archivos a

                    WHERE a.fecha_corte =
                          :fechaCorte

                    ORDER BY
                        a.fecha_importacion DESC,
                        a.id_central_archivo DESC

                    LIMIT 1
                ),

                datos_central AS
                (
                    SELECT DISTINCT ON
                    (
                        TRIM(d.documento),
                        TRIM(d.clasificacion_cartera)
                    )
                        d.id_central_archivo,

                        TRIM(d.documento)
                            AS documento,

                        TRIM(d.clasificacion_cartera)
                            AS clasificacion_cartera,

                        d.alertas_totales

                    FROM cartera.evaluaciones_cartera_central_datos d

                    INNER JOIN archivo_corte ac
                        ON ac.id_central_archivo =
                           d.id_central_archivo

                    ORDER BY
                        TRIM(d.documento),
                        TRIM(d.clasificacion_cartera),
                        d.id_central_dato DESC
                )

                SELECT
                    cc.id_cierre_cartera_credito
                        AS idCierreCarteraCredito,

                    cc.id_cartera_credito
                        AS idCarteraCredito,

                    cc.id_datos_personal
                        AS idDatosPersonal,

                    hv.documento,

                    cc.codigo_clasificacion_credito
                        AS codigoClasificacionCredito,

                    ac.id_central_archivo
                        AS idCentralArchivo,

                    ac.nombre_archivo
                        AS nombreArchivoCentral,

                    dc.alertas_totales
                        AS alertasTotales

                FROM cartera.cierres_cartera cierre

                INNER JOIN cartera.cierres_cartera_creditos cc
                    ON cc.id_cierre_cartera =
                       cierre.id_cierre_cartera

                INNER JOIN hoja_vida.cierres_hoja_vida cierre_hv
                    ON cierre_hv.fecha_corte =
                       cierre.fecha_corte

                   AND cierre_hv.estado = 'D'

                INNER JOIN hoja_vida.cierres_hoja_vida_personas hv
                    ON hv.id_cierre_hoja_vida =
                       cierre_hv.id_cierre_hoja_vida

                   AND hv.id_datos_personal =
                       cc.id_datos_personal

                LEFT JOIN archivo_corte ac
                    ON TRUE

                LEFT JOIN datos_central dc
                    ON dc.id_central_archivo =
                       ac.id_central_archivo

                   AND dc.documento =
                       TRIM(hv.documento)

                   AND dc.clasificacion_cartera =
                       TRIM(cc.codigo_clasificacion_credito)

                WHERE cierre.fecha_corte =
                      :fechaCorte
                
                  AND COALESCE(
                          cc.saldo_actual,
                          0
                      ) > 0

                ORDER BY
                    cc.id_agencia,
                    hv.documento,
                    cc.codigo_clasificacion_credito,
                    cc.id_cartera_credito
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        );

        return jdbc.query(
                sql,
                parametros,
                BeanPropertyRowMapper.newInstance(
                        Criterio711DatoDTO.class
                )
        );
    }

    // =========================================================
    // VALIDAR ARCHIVO PARA LA FECHA DE CORTE
    // =========================================================

    public boolean existeArchivoPorFechaCorte(
            LocalDate fechaCorte
    ) {

        String sql = """
                SELECT EXISTS
                (
                    SELECT 1

                    FROM cartera.evaluaciones_cartera_central_archivos a

                    WHERE a.fecha_corte =
                          :fechaCorte
                )
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "fechaCorte",
                                fechaCorte
                        );

        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Boolean.class
                )
        );
    }
}
