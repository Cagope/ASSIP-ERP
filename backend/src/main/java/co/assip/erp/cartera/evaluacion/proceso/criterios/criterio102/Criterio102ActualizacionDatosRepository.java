package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio102;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio102.dto.Criterio102DatoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class Criterio102ActualizacionDatosRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // OBTENER DATOS DEL CRITERIO 102
    //
    // Usa:
    // - créditos del cierre de cartera de la fecha evaluada;
    // - fotografía definitiva de Hoja de Vida del mismo corte.
    //
    // La consulta obtiene los datos de todas las agencias.
    // =========================================================

    public List<Criterio102DatoDTO> obtenerDatos(
            LocalDate fechaCorte
    ) {

        String sql = """
                SELECT
                    cc.id_cierre_cartera_credito
                        AS idCierreCarteraCredito,

                    cc.id_cartera_credito
                        AS idCarteraCredito,

                    cc.id_datos_personal
                        AS idDatosPersonal,

                    hv.documento,

                    hv.fecha_actualizacion
                        AS fechaActualizacionDatos

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

                WHERE cierre.fecha_corte =
                      :fechaCorte

                ORDER BY
                    cierre.id_agencia,
                    hv.documento,
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
                        Criterio102DatoDTO.class
                )
        );
    }

    // =========================================================
    // OBTENER PARÁMETRO 121
    // DÍAS MÁXIMO DE ACTUALIZACIÓN
    //
    // Para la evaluación institucional se usa la configuración
    // general de la agencia 1.
    // =========================================================

    public BigDecimal obtenerDiasMaximoActualizacion() {

        String sql = """
            SELECT
                p.valor_parametro

            FROM general.parametros p

            WHERE p.id_agencia = 1

              AND p.codigo_parametro = 121
            """;

        List<BigDecimal> resultados =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource(),
                        (
                                rs,
                                numeroFila
                        ) -> rs.getBigDecimal(
                                "valor_parametro"
                        )
                );

        return resultados.stream()
                .findFirst()
                .orElse(null);
    }

}