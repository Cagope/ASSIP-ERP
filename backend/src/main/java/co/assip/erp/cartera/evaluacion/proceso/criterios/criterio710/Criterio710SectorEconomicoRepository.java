package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio710;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio710.dto.Criterio710DatoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class Criterio710SectorEconomicoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // OBTENER DATOS DEL CRITERIO 710 - SECTOR ECONÓMICO
    //
    // El sector económico se toma directamente de la fotografía
    // histórica de Hoja de Vida correspondiente a la fecha de
    // corte:
    //
    // cierres_hoja_vida_personas.codigo_sector_economico
    //
    // No se consulta el catálogo de sectores para determinar
    // la regla. La parametrización del criterio 710 es la que
    // valida y puntúa el código recibido.
    // =========================================================

    public List<Criterio710DatoDTO> obtenerDatos(
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

                    hv.codigo_sector_economico
                        AS codigoSectorEconomico

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
                        Criterio710DatoDTO.class
                )
        );
    }
}