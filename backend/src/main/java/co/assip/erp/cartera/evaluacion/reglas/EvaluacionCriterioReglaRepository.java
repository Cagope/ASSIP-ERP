package co.assip.erp.cartera.evaluacion.reglas;

import co.assip.erp.cartera.evaluacion.reglas.dto.EvaluacionCriterioReglaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EvaluacionCriterioReglaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // LISTAR POR CRITERIO
    // =========================================================

    public List<EvaluacionCriterioReglaDTO> listarPorCriterio(
            Integer idEvaluacionCriterio
    ) {

        String sql = """
                SELECT
                    r.id_evaluacion_criterio_regla
                        AS idEvaluacionCriterioRegla,

                    r.id_evaluacion_criterio
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

                WHERE r.id_evaluacion_criterio =
                      :idEvaluacionCriterio

                ORDER BY
                    COALESCE(r.id_tipo_persona, 0),
                    r.orden,
                    r.codigo_regla
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCriterio",
                                idEvaluacionCriterio
                        );

        return jdbc.query(
                sql,
                parametros,
                BeanPropertyRowMapper.newInstance(
                        EvaluacionCriterioReglaDTO.class
                )
        );
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    public Optional<EvaluacionCriterioReglaDTO> buscarPorId(
            Integer idEvaluacionCriterioRegla
    ) {

        String sql = """
                SELECT
                    r.id_evaluacion_criterio_regla
                        AS idEvaluacionCriterioRegla,

                    r.id_evaluacion_criterio
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

                WHERE r.id_evaluacion_criterio_regla =
                      :idEvaluacionCriterioRegla
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCriterioRegla",
                                idEvaluacionCriterioRegla
                        );

        List<EvaluacionCriterioReglaDTO> resultado =
                jdbc.query(
                        sql,
                        parametros,
                        BeanPropertyRowMapper.newInstance(
                                EvaluacionCriterioReglaDTO.class
                        )
                );

        return resultado.stream().findFirst();
    }

    // =========================================================
    // VALIDAR CÓDIGO DUPLICADO
    // =========================================================

    public boolean existePorCodigo(
            Integer idEvaluacionCriterio,
            String codigoRegla,
            Short idTipoPersona,
            Integer excluirId
    ) {

        String sql = """
                SELECT EXISTS
                (
                    SELECT 1
                    FROM cartera.evaluacion_criterios_reglas
                    WHERE id_evaluacion_criterio =
                          :idEvaluacionCriterio

                      AND codigo_regla =
                          :codigoRegla

                      AND id_tipo_persona
                          IS NOT DISTINCT FROM :idTipoPersona

                      AND
                      (
                          :excluirId IS NULL
                          OR id_evaluacion_criterio_regla
                             <> :excluirId
                      )
                )
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCriterio",
                                idEvaluacionCriterio
                        )
                        .addValue(
                                "codigoRegla",
                                codigoRegla
                        )
                        .addValue(
                                "idTipoPersona",
                                idTipoPersona
                        )
                        .addValue(
                                "excluirId",
                                excluirId
                        );

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
            EvaluacionCriterioReglaDTO dto,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO cartera.evaluacion_criterios_reglas
                (
                    id_evaluacion_criterio,
                    codigo_regla,
                    nombre_regla,
                    tipo_regla,
                    id_tipo_persona,
                    valor_comparacion,
                    valor_desde,
                    valor_hasta,
                    puntaje,
                    orden,
                    aplica,
                    activo,
                    observaciones,
                    fk_seguridad_creacion,
                    fk_seguridad_edicion
                )
                VALUES
                (
                    :idEvaluacionCriterio,
                    :codigoRegla,
                    :nombreRegla,
                    :tipoRegla,
                    :idTipoPersona,
                    :valorComparacion,
                    :valorDesde,
                    :valorHasta,
                    :puntaje,
                    :orden,
                    :aplica,
                    :activo,
                    :observaciones,
                    :idUsuario,
                    :idUsuario
                )
                RETURNING id_evaluacion_criterio_regla
                """;

        MapSqlParameterSource parametros =
                construirParametros(dto)
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

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
            Integer idEvaluacionCriterioRegla,
            EvaluacionCriterioReglaDTO dto,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.evaluacion_criterios_reglas
                SET
                    id_evaluacion_criterio =
                        :idEvaluacionCriterio,

                    codigo_regla =
                        :codigoRegla,

                    nombre_regla =
                        :nombreRegla,

                    tipo_regla =
                        :tipoRegla,

                    id_tipo_persona =
                        :idTipoPersona,

                    valor_comparacion =
                        :valorComparacion,

                    valor_desde =
                        :valorDesde,

                    valor_hasta =
                        :valorHasta,

                    puntaje =
                        :puntaje,

                    orden =
                        :orden,

                    aplica =
                        :aplica,

                    activo =
                        :activo,

                    observaciones =
                        :observaciones,

                    fk_seguridad_edicion =
                        :idUsuario,

                    fecha_edicion =
                        CURRENT_TIMESTAMP

                WHERE id_evaluacion_criterio_regla =
                      :idEvaluacionCriterioRegla
                """;

        MapSqlParameterSource parametros =
                construirParametros(dto)
                        .addValue(
                                "idEvaluacionCriterioRegla",
                                idEvaluacionCriterioRegla
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // CAMBIAR ESTADO
    // =========================================================

    public int cambiarActivo(
            Integer idEvaluacionCriterioRegla,
            boolean activo,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.evaluacion_criterios_reglas
                SET
                    activo = :activo,
                    fk_seguridad_edicion = :idUsuario,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_evaluacion_criterio_regla =
                      :idEvaluacionCriterioRegla
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCriterioRegla",
                                idEvaluacionCriterioRegla
                        )
                        .addValue(
                                "activo",
                                activo
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // PARÁMETROS
    // =========================================================

    private MapSqlParameterSource construirParametros(
            EvaluacionCriterioReglaDTO dto
    ) {

        return new MapSqlParameterSource()
                .addValue(
                        "idEvaluacionCriterio",
                        dto.getIdEvaluacionCriterio()
                )
                .addValue(
                        "codigoRegla",
                        dto.getCodigoRegla()
                )
                .addValue(
                        "nombreRegla",
                        dto.getNombreRegla()
                )
                .addValue(
                        "tipoRegla",
                        dto.getTipoRegla()
                )
                .addValue(
                        "idTipoPersona",
                        dto.getIdTipoPersona()
                )
                .addValue(
                        "valorComparacion",
                        dto.getValorComparacion()
                )
                .addValue(
                        "valorDesde",
                        dto.getValorDesde()
                )
                .addValue(
                        "valorHasta",
                        dto.getValorHasta()
                )
                .addValue(
                        "puntaje",
                        dto.getPuntaje()
                )
                .addValue(
                        "orden",
                        dto.getOrden()
                )
                .addValue(
                        "aplica",
                        dto.getAplica()
                )
                .addValue(
                        "activo",
                        dto.getActivo()
                )
                .addValue(
                        "observaciones",
                        dto.getObservaciones()
                );
    }
}