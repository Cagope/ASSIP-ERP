package co.assip.erp.cartera.evaluacion.proceso.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EvaluacionCriterioResultadoDTO {

    // =========================================================
    // Crédito evaluado
    // =========================================================

    private Integer idEvaluacionCarteraCredito;

    private Integer idCierreCarteraCredito;

    private Integer idCarteraCredito;

    private Integer idDatosPersonal;

    private String documento;

    // =========================================================
    // Criterio aplicado
    // =========================================================

    private Integer idEvaluacionCriterio;

    private String codigoCriterio;

    private String nombreCriterio;

    // =========================================================
    // Regla aplicada
    // =========================================================

    private Integer idEvaluacionCriterioRegla;

    private String codigoRegla;

    private String nombreRegla;

    // =========================================================
    // Resultado
    // =========================================================

    private BigDecimal valorResultado;

    private String codigoResultado;

    private String descripcionResultado;

    private BigDecimal puntajeObtenido;

    private String observaciones;
}