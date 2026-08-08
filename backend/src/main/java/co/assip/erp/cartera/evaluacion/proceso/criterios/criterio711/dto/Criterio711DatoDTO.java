package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio711.dto;

import lombok.Data;

@Data
public class Criterio711DatoDTO {

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

    private String codigoClasificacionCredito;

    // =========================================================
    // Archivo de central de riesgo
    // =========================================================

    private Integer idCentralArchivo;

    private String nombreArchivoCentral;

    // =========================================================
    // Criterio 711 - Alertas Central de Riesgos
    // =========================================================

    /**
     * Cantidad total de alertas reportadas por la central
     * para la combinación exacta:
     *
     * documento
     * + clasificación del crédito
     * + fecha de corte
     *
     * Cuando no existe información permanece null.
     */
    private Integer alertasTotales;
}