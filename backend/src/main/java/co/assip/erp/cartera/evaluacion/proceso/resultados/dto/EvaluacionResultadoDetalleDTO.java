package co.assip.erp.cartera.evaluacion.proceso.resultados.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EvaluacionResultadoDetalleDTO {

    // =========================================================
    // IDENTIFICACIÓN DEL DETALLE
    // =========================================================

    private Integer idEvaluacionCarteraCreditoDetalle;

    private Integer idEvaluacionCarteraCredito;


    // =========================================================
    // CRITERIO
    // =========================================================

    private Integer idEvaluacionCriterio;

    private String codigoCriterio;

    private String nombreCriterio;

    private Integer ordenEvaluacion;

    private BigDecimal puntajeMaximo;


    // =========================================================
    // REGLA APLICADA
    // =========================================================

    private Integer idEvaluacionCriterioRegla;

    private String codigoRegla;

    private String nombreRegla;


    // =========================================================
    // RESULTADO DEL CRITERIO
    // =========================================================

    private BigDecimal valorResultado;

    private String codigoResultado;

    private String descripcionResultado;

    private BigDecimal puntajeObtenido;

    private String observaciones;
}