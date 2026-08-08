package co.assip.erp.cartera.evaluacion.proceso.reglas;

import co.assip.erp.cartera.evaluacion.reglas.dto.EvaluacionCriterioReglaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EvaluacionReglasRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // LISTAR REGLAS ACTIVAS DE UN CRITERIO
    //
    // No depende del código de una regla específica.
    // Devuelve todas las reglas configuradas, activas y
    // aplicables para que el selector determine cuál corresponde.
    // =========================================================

    public List<EvaluacionCriterioReglaDTO> listarReglasActivas(
            String codigoCriterio,
            Short idTipoPersona
    ) {

        String sql = """
                SELECT
                    r.id_evaluacion_criterio_regla
                        AS idEvaluacionCriterioRegla,

                    c.id_evaluacion_criterio
                        AS idEvaluacionCriterio,

                    c.codigo_criterio
                        AS codigoCriterio,

                    c.nombre_criterio
                        AS nombreCriterio,

                    r.codigo_regla
                        AS codigoRegla,

                    r.nombre_regla
                        AS nombreRegla,

                    r.tipo_regla
                        AS tipoRegla,

                    r.id_tipo_persona
                        AS idTipoPersona,

                    r.valor_comparacion
                        AS valorComparacion,

                    r.valor_desde
                        AS valorDesde,

                    r.valor_hasta
                        AS valorHasta,

                    r.puntaje,

                    r.orden,

                    r.aplica,

                    r.activo,

                    r.observaciones,

                    r.fk_seguridad_creacion
                        AS fkSeguridadCreacion,

                    r.fecha_creacion
                        AS fechaCreacion,

                    r.fk_seguridad_edicion
                        AS fkSeguridadEdicion,

                    r.fecha_edicion
                        AS fechaEdicion

                FROM cartera.evaluacion_criterios_reglas r

                INNER JOIN cartera.evaluacion_criterios c
                    ON c.id_evaluacion_criterio =
                       r.id_evaluacion_criterio

                WHERE c.codigo_criterio =
                      :codigoCriterio

                  AND c.aplica = TRUE

                  AND c.activo = TRUE

                  AND r.aplica = TRUE

                  AND r.activo = TRUE

                  AND
                  (
                      r.id_tipo_persona IS NULL

                      OR r.id_tipo_persona =
                         :idTipoPersona
                  )

                ORDER BY
                    CASE
                        WHEN r.id_tipo_persona IS NOT NULL
                        THEN 0
                        ELSE 1
                    END,

                    r.orden,

                    r.id_evaluacion_criterio_regla
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "codigoCriterio",
                                codigoCriterio
                        )
                        .addValue(
                                "idTipoPersona",
                                idTipoPersona
                        );

        return jdbc.query(
                sql,
                parametros,
                BeanPropertyRowMapper.newInstance(
                        EvaluacionCriterioReglaDTO.class
                )
        );
    }
}