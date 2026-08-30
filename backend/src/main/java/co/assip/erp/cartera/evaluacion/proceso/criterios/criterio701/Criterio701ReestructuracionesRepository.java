package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio701;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio701.dto.Criterio701DatoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class Criterio701ReestructuracionesRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // OBTENER DATOS DEL CRITERIO 701 - REESTRUCTURACIONES
    //
    // La condiciÃ³n de reestructuraciÃ³n se toma directamente
    // de la fotografÃ­a histÃ³rica del crÃ©dito:
    //
    // cierres_cartera_creditos.credito_reestructurado
    //
    // false = sin reestructuraciÃ³n
    // true  = con reestructuraciÃ³n
    //
    // Los demÃ¡s campos relacionados con la reestructuraciÃ³n
    // se recuperan Ãºnicamente como informaciÃ³n complementaria.
    // =========================================================

    public List<Criterio701DatoDTO> obtenerDatos(
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

                    cc.credito_reestructurado
                        AS creditoReestructurado,

                    cc.codigo_modificacion_credito
                        AS codigoModificacionCredito,

                    cc.fecha_reestructuracion
                        AS fechaReestructuracion,

                    cc.edad_reestructurado
                        AS edadReestructurado

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
                        Criterio701DatoDTO.class
                )
        );
    }
}
