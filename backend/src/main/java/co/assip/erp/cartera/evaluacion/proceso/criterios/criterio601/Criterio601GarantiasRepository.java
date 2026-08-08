package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio601;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio601.dto.Criterio601DatoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class Criterio601GarantiasRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // OBTENER DATOS DEL CRITERIO 601 - GARANTÍAS
    //
    // La garantía se toma directamente de la fotografía
    // histórica del crédito:
    //
    // cierres_cartera_creditos.codigo_garantia_credito
    //
    // El catálogo cartera.garantias_creditos se utiliza
    // únicamente para complementar la descripción y el tipo.
    // =========================================================

    public List<Criterio601DatoDTO> obtenerDatos(
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

                    cc.codigo_garantia_credito
                        AS codigoGarantiaCredito,

                    g.descripcion_garantia_credito
                        AS descripcionGarantiaCredito,

                    g.tipo_garantia
                        AS tipoGarantia

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

                LEFT JOIN cartera.garantias_creditos g
                    ON g.codigo_garantia_credito =
                       cc.codigo_garantia_credito

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
                        Criterio601DatoDTO.class
                )
        );
    }
}