package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio701.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Criterio701DatoDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCierreCarteraCredito;

    private Integer idCarteraCredito;

    private Integer idDatosPersonal;

    private String documento;

    // =========================================================
    // Criterio 701 - Reestructuraciones
    // =========================================================

    /**
     * Indicador histórico almacenado en la fotografía
     * del crédito.
     *
     * false = crédito sin reestructuración
     * true  = crédito con reestructuración
     */
    private Boolean creditoReestructurado;

    // =========================================================
    // Información complementaria
    // =========================================================

    /**
     * Código de modificación registrado en la fotografía
     * histórica del crédito.
     *
     * No participa directamente en la selección de la regla
     * del criterio 701.
     */
    private String codigoModificacionCredito;

    /**
     * Fecha de reestructuración del crédito.
     *
     * Se conserva como información complementaria para
     * trazabilidad y observaciones.
     */
    private LocalDate fechaReestructuracion;

    /**
     * Edad de riesgo asociada a la reestructuración.
     *
     * No participa directamente en la selección de la regla.
     */
    private String edadReestructurado;
}