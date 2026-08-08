package co.assip.erp.cartera.evaluacion.proceso.motor.dto;

import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCriterioResultadoDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class EvaluacionCreditoResultadoDTO {

    // =========================================================
    // IDENTIFICACIÓN DE LA EVALUACIÓN
    // =========================================================

    private Integer idEvaluacionCartera;

    /**
     * Se asigna después de insertar el resultado consolidado
     * en cartera.evaluaciones_cartera_creditos.
     */
    private Integer idEvaluacionCarteraCredito;

    // =========================================================
    // IDENTIFICACIÓN DEL CRÉDITO HISTÓRICO
    // =========================================================

    private Integer idCierreCarteraCredito;

    private Integer idCarteraCredito;

    private Integer idDatosPersonal;

    private String documento;

    private String pagareCartera;

    // =========================================================
    // CLASIFICACIÓN
    //
    // Es fundamental para la alineación posterior:
    //
    // idDatosPersonal + codigoClasificacionCredito
    //
    // Todos los créditos de una misma persona y clasificación
    // deben terminar alineados a la peor categoría resultante
    // del grupo.
    // =========================================================

    private String codigoClasificacionCredito;

    // =========================================================
    // INFORMACIÓN HISTÓRICA DE RIESGO
    // =========================================================

    /**
     * Indica si el crédito ya venía evaluado en el cierre.
     *
     * Equivale funcionalmente al antiguo ESTADO_SARC:
     *
     * false = no habilita disminución de categoría
     * true  = puede intentar disminuir categoría cuando
     *         el puntaje sea favorable.
     */
    private Boolean creditoEvaluado;

    /**
     * Edad de riesgo que tenía el crédito antes de ejecutar
     * esta nueva evaluación.
     *
     * Proviene de:
     *
     * cierres_cartera_creditos.edad_de_riesgo
     */
    private String edadRiesgoAnterior;

    /**
     * Edad inicial utilizada como base del proceso.
     *
     * Proviene de:
     *
     * cierres_cartera_creditos.edad_riesgo_inicial
     */
    private String edadRiesgoInicial;

    /**
     * Edad determinada por mora al corte.
     *
     * Proviene de:
     *
     * cierres_cartera_creditos.edad_de_mora
     */
    private String edadMora;

    // =========================================================
    // RESULTADOS DE LOS CRITERIOS
    // =========================================================

    /**
     * Contendrá los resultados de los criterios:
     *
     * 102
     * 201
     * 401
     * 402
     * 403
     * 502
     * 601
     * 701
     * 710
     * 711
     */
    private List<EvaluacionCriterioResultadoDTO> criterios =
            new ArrayList<>();

    // =========================================================
    // PUNTAJE TOTAL
    // =========================================================

    /**
     * Suma de puntaje_obtenido de todos los criterios
     * aplicados al crédito.
     */
    private BigDecimal puntajeTotal =
            BigDecimal.ZERO;

    // =========================================================
    // EDAD CALCULADA INDIVIDUALMENTE
    // =========================================================

    /**
     * Resultado directo del análisis del puntaje.
     *
     * Todavía no representa necesariamente la categoría final
     * porque falta aplicar controles individuales y alineación.
     */
    private String edadRiesgoCalculada;

    // =========================================================
    // EDAD DESPUÉS DE REGLAS INDIVIDUALES
    // =========================================================

    /**
     * Resultado después de aplicar:
     *
     * - edad inicial;
     * - edad de mora;
     * - puntaje mínimo favorable;
     * - indicador credito_evaluado.
     *
     * Esta es la edad que participa en la alineación por
     * persona + clasificación.
     */
    private String edadRiesgoArrastre;

    // =========================================================
    // EDAD FINAL
    // =========================================================

    /**
     * Categoría definitiva después de alinear todos los
     * créditos de:
     *
     * idDatosPersonal + codigoClasificacionCredito
     *
     * a la peor categoría de riesgo resultante del grupo.
     */
    private String edadRiesgoFinal;

    // =========================================================
    // ACCIÓN FINAL
    //
    // Se determina solamente después de la alineación.
    //
    // R = Recalificar
    // H = Habilitar
    // M = Mantener
    // =========================================================

    private String accionEvaluacion;

    // =========================================================
    // COMENTARIO
    // =========================================================

    /**
     * Explicación consolidada del resultado del crédito.
     *
     * Puede incluir:
     *
     * - puntaje total;
     * - resultado individual;
     * - efecto de mora;
     * - alineación por persona y clasificación;
     * - acción final.
     */
    private String comentarioEvaluacion;
}