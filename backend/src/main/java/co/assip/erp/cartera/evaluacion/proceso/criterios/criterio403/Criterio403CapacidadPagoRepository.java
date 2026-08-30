package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio403;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio403.dto.Criterio403DatoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class Criterio403CapacidadPagoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // OBTENER DATOS DEL CRITERIO 403 - CAPACIDAD DE PAGO
    //
    // Los datos econÃ³micos se toman exclusivamente de la
    // fotografÃ­a histÃ³rica de Hoja de Vida correspondiente
    // a la misma fecha de corte de la cartera.
    //
    // La fotografÃ­a ya contiene:
    //
    // ingresos_totales
    // egresos_totales
    //
    // Por lo tanto, estos valores no se reconstruyen a partir
    // de los diferentes conceptos econÃ³micos.
    // =========================================================

    public List<Criterio403DatoDTO> obtenerDatos(
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

                    hv.ingresos_totales
                        AS ingresosTotales,

                    hv.egresos_totales
                        AS egresosTotales

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

                   AND COALESCE(
                           cc.saldo_actual,
                           0
                       ) > 0

                ORDER BY
                    cc.id_agencia,
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
                        Criterio403DatoDTO.class
                )
        );
    }
}
