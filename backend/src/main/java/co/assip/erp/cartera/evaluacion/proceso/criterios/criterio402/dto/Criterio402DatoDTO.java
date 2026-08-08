package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio402.dto;

import lombok.Data;

@Data
public class Criterio402DatoDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCierreCarteraCredito;

    private Integer idCarteraCredito;

    private Integer idDatosPersonal;

    private String documento;

    // =========================================================
    // Crédito
    // =========================================================

    /**
     * Clasificación del crédito almacenada en el cierre
     * de cartera.
     *
     * Ejemplos:
     * C = Comercial
     * M = Microcrédito
     * S = Consumo u otra clasificación institucional
     */
    private String codigoClasificacionCredito;

    // =========================================================
    // Archivo de central de riesgo
    // =========================================================

    private Integer idCentralArchivo;

    private String nombreArchivoCentral;

    // =========================================================
    // Criterio 402 - Historial Central de Riesgo
    // =========================================================

    /**
     * Calificación reportada por la central de riesgo para
     * la combinación exacta:
     *
     * documento
     * + clasificación del crédito
     * + fecha de corte
     *
     * Valores esperados:
     * A, B, C, D, E, F o CASTIGADA.
     *
     * Cuando no existe coincidencia permanece null.
     */
    private String calificacionCentralRiesgo;
}