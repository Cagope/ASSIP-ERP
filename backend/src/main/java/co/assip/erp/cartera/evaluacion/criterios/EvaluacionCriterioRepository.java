package co.assip.erp.cartera.evaluacion.criterios;

import co.assip.erp.cartera.evaluacion.criterios.dto.EvaluacionCriterioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EvaluacionCriterioRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // LISTAR
    // =========================================================

    public List<EvaluacionCriterioDTO> listar() {

        String sql = """
                SELECT
                    id_evaluacion_criterio AS idEvaluacionCriterio,
                    codigo_proceso AS codigoProceso,
                    codigo_criterio AS codigoCriterio,
                    nombre_criterio AS nombreCriterio,
                    descripcion_criterio AS descripcionCriterio,
                    nivel_aplicacion AS nivelAplicacion,
                    tipo_comparacion AS tipoComparacion,
                    orden_evaluacion AS ordenEvaluacion,
                    puntaje_maximo AS puntajeMaximo,
                    aplica,
                    activo,
                    fk_seguridad_creacion AS fkSeguridadCreacion,
                    fecha_creacion AS fechaCreacion,
                    fk_seguridad_edicion AS fkSeguridadEdicion,
                    fecha_edicion AS fechaEdicion
                FROM cartera.evaluacion_criterios
                ORDER BY
                    orden_evaluacion,
                    codigo_criterio
                """;

        return jdbc.query(
                sql,
                BeanPropertyRowMapper.newInstance(
                        EvaluacionCriterioDTO.class
                )
        );
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    public Optional<EvaluacionCriterioDTO> buscarPorId(
            Integer idEvaluacionCriterio
    ) {

        String sql = """
                SELECT
                    id_evaluacion_criterio AS idEvaluacionCriterio,
                    codigo_proceso AS codigoProceso,
                    codigo_criterio AS codigoCriterio,
                    nombre_criterio AS nombreCriterio,
                    descripcion_criterio AS descripcionCriterio,
                    nivel_aplicacion AS nivelAplicacion,
                    tipo_comparacion AS tipoComparacion,
                    orden_evaluacion AS ordenEvaluacion,
                    puntaje_maximo AS puntajeMaximo,
                    aplica,
                    activo,
                    fk_seguridad_creacion AS fkSeguridadCreacion,
                    fecha_creacion AS fechaCreacion,
                    fk_seguridad_edicion AS fkSeguridadEdicion,
                    fecha_edicion AS fechaEdicion
                FROM cartera.evaluacion_criterios
                WHERE id_evaluacion_criterio = :idEvaluacionCriterio
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCriterio",
                                idEvaluacionCriterio
                        );

        List<EvaluacionCriterioDTO> resultado =
                jdbc.query(
                        sql,
                        parametros,
                        BeanPropertyRowMapper.newInstance(
                                EvaluacionCriterioDTO.class
                        )
                );

        return resultado.stream().findFirst();
    }

    // =========================================================
    // EXISTE POR CÓDIGO
    // =========================================================

    public boolean existePorCodigo(
            String codigoProceso,
            String codigoCriterio,
            Integer excluirId
    ) {

        String sql = """
                SELECT EXISTS
                (
                    SELECT 1
                    FROM cartera.evaluacion_criterios
                    WHERE codigo_proceso = :codigoProceso
                      AND codigo_criterio = :codigoCriterio
                      AND
                      (
                          :excluirId IS NULL
                          OR id_evaluacion_criterio <> :excluirId
                      )
                )
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue("codigoProceso", codigoProceso)
                        .addValue("codigoCriterio", codigoCriterio)
                        .addValue("excluirId", excluirId);

        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        sql,
                        parametros,
                        Boolean.class
                )
        );
    }

    // =========================================================
    // CREAR
    // =========================================================

    public Integer crear(
            EvaluacionCriterioDTO dto,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO cartera.evaluacion_criterios
                (
                    codigo_proceso,
                    codigo_criterio,
                    nombre_criterio,
                    descripcion_criterio,
                    nivel_aplicacion,
                    tipo_comparacion,
                    orden_evaluacion,
                    puntaje_maximo,
                    aplica,
                    activo,
                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                )
                VALUES
                (
                    :codigoProceso,
                    :codigoCriterio,
                    :nombreCriterio,
                    :descripcionCriterio,
                    :nivelAplicacion,
                    :tipoComparacion,
                    :ordenEvaluacion,
                    :puntajeMaximo,
                    :aplica,
                    :activo,
                    :idUsuario,
                    :idUsuario
                )
                RETURNING id_evaluacion_criterio
                """;

        MapSqlParameterSource parametros =
                construirParametros(dto)
                        .addValue("idUsuario", idUsuario);

        return jdbc.queryForObject(
                sql,
                parametros,
                Integer.class
        );
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public int actualizar(
            Integer idEvaluacionCriterio,
            EvaluacionCriterioDTO dto,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.evaluacion_criterios
                SET
                    codigo_proceso = :codigoProceso,
                    codigo_criterio = :codigoCriterio,
                    nombre_criterio = :nombreCriterio,
                    descripcion_criterio = :descripcionCriterio,
                    nivel_aplicacion = :nivelAplicacion,
                    tipo_comparacion = :tipoComparacion,
                    orden_evaluacion = :ordenEvaluacion,
                    puntaje_maximo = :puntajeMaximo,
                    aplica = :aplica,
                    activo = :activo,
                    fk_seguridad_edicion = :idUsuario,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_evaluacion_criterio = :idEvaluacionCriterio
                """;

        MapSqlParameterSource parametros =
                construirParametros(dto)
                        .addValue("idUsuario", idUsuario)
                        .addValue(
                                "idEvaluacionCriterio",
                                idEvaluacionCriterio
                        );

        return jdbc.update(sql, parametros);
    }

    // =========================================================
    // CAMBIAR ESTADO
    // =========================================================

    public int cambiarActivo(
            Integer idEvaluacionCriterio,
            boolean activo,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.evaluacion_criterios
                SET
                    activo = :activo,
                    fk_seguridad_edicion = :idUsuario,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_evaluacion_criterio = :idEvaluacionCriterio
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCriterio",
                                idEvaluacionCriterio
                        )
                        .addValue("activo", activo)
                        .addValue("idUsuario", idUsuario);

        return jdbc.update(sql, parametros);
    }

    // =========================================================
    // PARÁMETROS
    // =========================================================

    private MapSqlParameterSource construirParametros(
            EvaluacionCriterioDTO dto
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        "codigoProceso",
                        dto.getCodigoProceso()
                )
                .addValue(
                        "codigoCriterio",
                        dto.getCodigoCriterio()
                )
                .addValue(
                        "nombreCriterio",
                        dto.getNombreCriterio()
                )
                .addValue(
                        "descripcionCriterio",
                        dto.getDescripcionCriterio()
                )
                .addValue(
                        "nivelAplicacion",
                        dto.getNivelAplicacion()
                )
                .addValue(
                        "tipoComparacion",
                        dto.getTipoComparacion()
                )
                .addValue(
                        "ordenEvaluacion",
                        dto.getOrdenEvaluacion()
                )
                .addValue(
                        "puntajeMaximo",
                        dto.getPuntajeMaximo()
                )
                .addValue(
                        "aplica",
                        dto.getAplica()
                )
                .addValue(
                        "activo",
                        dto.getActivo()
                );
    }
}