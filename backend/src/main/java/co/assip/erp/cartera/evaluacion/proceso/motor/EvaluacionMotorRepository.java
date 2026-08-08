package co.assip.erp.cartera.evaluacion.proceso.motor;

import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCriterioResultadoDTO;
import co.assip.erp.cartera.evaluacion.proceso.motor.dto.EvaluacionCreditoResultadoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class EvaluacionMotorRepository {

    private final NamedParameterJdbcTemplate jdbc;

    // =========================================================
    // LIMPIAR RESULTADOS ANTERIORES DE LA EVALUACIÓN
    //
    // Se utiliza cuando una evaluación que todavía está en
    // estado P se vuelve a ejecutar.
    //
    // Primero se eliminan los detalles porque dependen de
    // evaluaciones_cartera_creditos.
    // =========================================================

    public int eliminarDetallesPorEvaluacion(
            Integer idEvaluacionCartera
    ) {

        String sql = """
                DELETE FROM cartera.evaluaciones_cartera_creditos_detalle d

                USING cartera.evaluaciones_cartera_creditos ec

                WHERE d.id_evaluacion_cartera_credito =
                      ec.id_evaluacion_cartera_credito

                  AND ec.id_evaluacion_cartera =
                      :idEvaluacionCartera
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCartera",
                                idEvaluacionCartera
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // ELIMINAR RESULTADOS CONSOLIDADOS ANTERIORES
    // =========================================================

    public int eliminarCreditosPorEvaluacion(
            Integer idEvaluacionCartera
    ) {

        String sql = """
                DELETE FROM cartera.evaluaciones_cartera_creditos

                WHERE id_evaluacion_cartera =
                      :idEvaluacionCartera
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCartera",
                                idEvaluacionCartera
                        );

        return jdbc.update(
                sql,
                parametros
        );
    }

    // =========================================================
    // REINICIAR TOTALES DE CABECERA
    // =========================================================

    public int reiniciarTotalesEvaluacion(
            Integer idEvaluacionCartera,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.evaluaciones_cartera

                SET cantidad_creditos = 0,
                    cantidad_asociados = 0,
                    saldo_total_evaluado = 0,
                    cantidad_recalificados = 0,
                    cantidad_habilitados = 0,
                    cantidad_mantenidos = 0,
                    fk_seguridad_edicion = :idUsuario,
                    fecha_edicion = CURRENT_TIMESTAMP

                WHERE id_evaluacion_cartera =
                      :idEvaluacionCartera

                  AND estado = 'P'
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCartera",
                                idEvaluacionCartera
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
    // GUARDAR RESULTADO CONSOLIDADO DEL CRÉDITO
    // =========================================================

    public Integer guardarCredito(
            EvaluacionCreditoResultadoDTO resultado,
            Integer idUsuario
    ) {

        String sql = """
                INSERT INTO cartera.evaluaciones_cartera_creditos
                (
                    id_evaluacion_cartera,
                    id_cierre_cartera_credito,
                    id_cartera_credito,

                    puntaje_total,

                    edad_mora,
                    edad_riesgo_anterior,
                    edad_riesgo_inicial,
                    edad_riesgo_calculada,
                    edad_riesgo_arrastre,
                    edad_riesgo_final,

                    accion_evaluacion,
                    comentario_evaluacion,

                    fk_seguridad_creacion,
                    fecha_creacion,
                    fk_seguridad_edicion,
                    fecha_edicion
                )
                VALUES
                (
                    :idEvaluacionCartera,
                    :idCierreCarteraCredito,
                    :idCarteraCredito,

                    :puntajeTotal,

                    :edadMora,
                    :edadRiesgoAnterior,
                    :edadRiesgoInicial,
                    :edadRiesgoCalculada,
                    :edadRiesgoArrastre,
                    :edadRiesgoFinal,

                    :accionEvaluacion,
                    :comentarioEvaluacion,

                    :idUsuario,
                    CURRENT_TIMESTAMP,
                    :idUsuario,
                    CURRENT_TIMESTAMP
                )
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCartera",
                                resultado.getIdEvaluacionCartera()
                        )
                        .addValue(
                                "idCierreCarteraCredito",
                                resultado.getIdCierreCarteraCredito()
                        )
                        .addValue(
                                "idCarteraCredito",
                                resultado.getIdCarteraCredito()
                        )
                        .addValue(
                                "puntajeTotal",
                                valorDecimal(
                                        resultado.getPuntajeTotal()
                                )
                        )
                        .addValue(
                                "edadMora",
                                limpiar(
                                        resultado.getEdadMora()
                                )
                        )
                        .addValue(
                                "edadRiesgoAnterior",
                                limpiar(
                                        resultado.getEdadRiesgoAnterior()
                                )
                        )
                        .addValue(
                                "edadRiesgoInicial",
                                limpiar(
                                        resultado.getEdadRiesgoInicial()
                                )
                        )
                        .addValue(
                                "edadRiesgoCalculada",
                                limpiar(
                                        resultado.getEdadRiesgoCalculada()
                                )
                        )
                        .addValue(
                                "edadRiesgoArrastre",
                                limpiar(
                                        resultado.getEdadRiesgoArrastre()
                                )
                        )
                        .addValue(
                                "edadRiesgoFinal",
                                limpiar(
                                        resultado.getEdadRiesgoFinal()
                                )
                        )
                        .addValue(
                                "accionEvaluacion",
                                limpiar(
                                        resultado.getAccionEvaluacion()
                                )
                        )
                        .addValue(
                                "comentarioEvaluacion",
                                limitar(
                                        resultado.getComentarioEvaluacion(),
                                        1000
                                )
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        KeyHolder keyHolder =
                new GeneratedKeyHolder();

        int filas =
                jdbc.update(
                        sql,
                        parametros,
                        keyHolder,
                        new String[]{
                                "id_evaluacion_cartera_credito"
                        }
                );

        if (filas == 0) {
            throw new IllegalStateException(
                    "No fue posible guardar el resultado "
                            + "consolidado del crédito "
                            + resultado.getIdCarteraCredito()
                            + "."
            );
        }

        Number idGenerado =
                keyHolder.getKey();

        if (idGenerado == null) {
            Map<String, Object> keys =
                    keyHolder.getKeys();

            if (
                    keys != null
                            && keys.get(
                            "id_evaluacion_cartera_credito"
                    ) instanceof Number numero
            ) {
                idGenerado = numero;
            }
        }

        if (idGenerado == null) {
            throw new IllegalStateException(
                    "El resultado del crédito "
                            + resultado.getIdCarteraCredito()
                            + " fue guardado, pero no fue posible "
                            + "obtener su identificador."
            );
        }

        Integer idEvaluacionCarteraCredito =
                idGenerado.intValue();

        resultado.setIdEvaluacionCarteraCredito(
                idEvaluacionCarteraCredito
        );

        return idEvaluacionCarteraCredito;
    }

    // =========================================================
    // GUARDAR DETALLES DE CRITERIOS
    //
    // Se insertan en lote los 10 criterios correspondientes
    // al crédito.
    // =========================================================

    public void guardarDetalles(
            Integer idEvaluacionCarteraCredito,
            List<EvaluacionCriterioResultadoDTO> detalles,
            Integer idUsuario
    ) {

        if (
                detalles == null
                        || detalles.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "El crédito evaluado debe tener "
                            + "resultados de criterios."
            );
        }

        String sql = """
                INSERT INTO cartera.evaluaciones_cartera_creditos_detalle
                (
                    id_evaluacion_cartera_credito,
                    id_evaluacion_criterio,
                    id_evaluacion_criterio_regla,

                    valor_resultado,
                    codigo_resultado,
                    descripcion_resultado,
                    puntaje_obtenido,
                    observaciones,

                    fk_seguridad_creacion,
                    fecha_creacion,
                    fk_seguridad_edicion,
                    fecha_edicion
                )
                VALUES
                (
                    :idEvaluacionCarteraCredito,
                    :idEvaluacionCriterio,
                    :idEvaluacionCriterioRegla,

                    :valorResultado,
                    :codigoResultado,
                    :descripcionResultado,
                    :puntajeObtenido,
                    :observaciones,

                    :idUsuario,
                    CURRENT_TIMESTAMP,
                    :idUsuario,
                    CURRENT_TIMESTAMP
                )
                """;

        MapSqlParameterSource[] parametros =
                detalles.stream()
                        .map(
                                detalle ->
                                        construirParametrosDetalle(
                                                idEvaluacionCarteraCredito,
                                                detalle,
                                                idUsuario
                                        )
                        )
                        .toArray(
                                MapSqlParameterSource[]::new
                        );

        int[] resultados =
                jdbc.batchUpdate(
                        sql,
                        parametros
                );

        if (resultados.length != detalles.size()) {
            throw new IllegalStateException(
                    "No fue posible guardar todos los criterios "
                            + "del crédito evaluado "
                            + idEvaluacionCarteraCredito
                            + "."
            );
        }
    }

    // =========================================================
    // CONSTRUIR PARÁMETROS DEL DETALLE
    // =========================================================

    private MapSqlParameterSource construirParametrosDetalle(
            Integer idEvaluacionCarteraCredito,
            EvaluacionCriterioResultadoDTO detalle,
            Integer idUsuario
    ) {

        validarDetalle(
                detalle
        );

        return new MapSqlParameterSource()
                .addValue(
                        "idEvaluacionCarteraCredito",
                        idEvaluacionCarteraCredito
                )
                .addValue(
                        "idEvaluacionCriterio",
                        detalle.getIdEvaluacionCriterio()
                )
                .addValue(
                        "idEvaluacionCriterioRegla",
                        detalle.getIdEvaluacionCriterioRegla()
                )
                .addValue(
                        "valorResultado",
                        detalle.getValorResultado()
                )
                .addValue(
                        "codigoResultado",
                        limitar(
                                detalle.getCodigoResultado(),
                                30
                        )
                )
                .addValue(
                        "descripcionResultado",
                        limitar(
                                detalle.getDescripcionResultado(),
                                250
                        )
                )
                .addValue(
                        "puntajeObtenido",
                        valorDecimal(
                                detalle.getPuntajeObtenido()
                        )
                )
                .addValue(
                        "observaciones",
                        limitar(
                                detalle.getObservaciones(),
                                1000
                        )
                )
                .addValue(
                        "idUsuario",
                        idUsuario
                );
    }

    // =========================================================
    // ACTUALIZAR RESUMEN DEFINITIVO DE LA EJECUCIÓN
    //
    // La evaluación continúa en estado P.
    //
    // Marcarla como D sigue siendo responsabilidad del
    // proceso explícito "marcar definitiva".
    // =========================================================

    public int actualizarResumenEvaluacion(
            Integer idEvaluacionCartera,
            Integer cantidadCreditos,
            Integer cantidadAsociados,
            BigDecimal saldoTotalEvaluado,
            Integer cantidadRecalificados,
            Integer cantidadHabilitados,
            Integer cantidadMantenidos,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cartera.evaluaciones_cartera

                SET fecha_ejecucion = CURRENT_TIMESTAMP,

                    cantidad_creditos =
                        :cantidadCreditos,

                    cantidad_asociados =
                        :cantidadAsociados,

                    saldo_total_evaluado =
                        :saldoTotalEvaluado,

                    cantidad_recalificados =
                        :cantidadRecalificados,

                    cantidad_habilitados =
                        :cantidadHabilitados,

                    cantidad_mantenidos =
                        :cantidadMantenidos,

                    fk_seguridad_edicion =
                        :idUsuario,

                    fecha_edicion =
                        CURRENT_TIMESTAMP

                WHERE id_evaluacion_cartera =
                      :idEvaluacionCartera

                  AND estado = 'P'
                """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue(
                                "idEvaluacionCartera",
                                idEvaluacionCartera
                        )
                        .addValue(
                                "cantidadCreditos",
                                cantidadCreditos
                        )
                        .addValue(
                                "cantidadAsociados",
                                cantidadAsociados
                        )
                        .addValue(
                                "saldoTotalEvaluado",
                                valorDecimal(
                                        saldoTotalEvaluado
                                )
                        )
                        .addValue(
                                "cantidadRecalificados",
                                cantidadRecalificados
                        )
                        .addValue(
                                "cantidadHabilitados",
                                cantidadHabilitados
                        )
                        .addValue(
                                "cantidadMantenidos",
                                cantidadMantenidos
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
    // VALIDAR DETALLE
    // =========================================================

    private void validarDetalle(
            EvaluacionCriterioResultadoDTO detalle
    ) {

        if (detalle == null) {
            throw new IllegalArgumentException(
                    "El resultado del criterio es obligatorio."
            );
        }

        if (detalle.getIdEvaluacionCriterio() == null) {
            throw new IllegalArgumentException(
                    "El identificador del criterio evaluado "
                            + "es obligatorio."
            );
        }

        if (detalle.getIdEvaluacionCriterioRegla() == null) {
            throw new IllegalArgumentException(
                    "El identificador de la regla aplicada "
                            + "es obligatorio."
            );
        }

        if (
                detalle.getDescripcionResultado() == null
                        || detalle.getDescripcionResultado().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "La descripción del resultado del criterio "
                            + detalle.getCodigoCriterio()
                            + " es obligatoria."
            );
        }
    }

    // =========================================================
    // APOYO
    // =========================================================

    private BigDecimal valorDecimal(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private String limpiar(
            String valor
    ) {

        if (valor == null) {
            return null;
        }

        String resultado =
                valor.trim();

        return resultado.isEmpty()
                ? null
                : resultado;
    }

    private String limitar(
            String valor,
            int longitudMaxima
    ) {

        String resultado =
                limpiar(
                        valor
                );

        if (resultado == null) {
            return null;
        }

        if (resultado.length() <= longitudMaxima) {
            return resultado;
        }

        return resultado.substring(
                0,
                longitudMaxima
        );
    }
}