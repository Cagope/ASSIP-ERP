package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio601.dto;

import lombok.Data;

@Data
public class Criterio601DatoDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCierreCarteraCredito;

    private Integer idCarteraCredito;

    private Integer idDatosPersonal;

    private String documento;

    // =========================================================
    // Criterio 601 - Garantías
    // =========================================================

    /**
     * Código de garantía almacenado en la fotografía
     * histórica del crédito.
     *
     * Ejemplos:
     * 1  = Garantía no idónea
     * 2  = Hipotecaria
     * 3  = Prendaria
     * 6  = Contratos de fiducia
     * 8  = Pignoración de rentas
     * 9  = Otras garantías idóneas
     * 10 = Depósitos de dinero en garantía
     * 11 = Garantía soberana de la Nación
     * 12 = Fondos de garantías
     * 13 = Derechos de cobro
     * 14 = Fiducia sobre inmuebles
     * 15 = Sin garantía
     */
    private String codigoGarantiaCredito;

    /**
     * Descripción obtenida del catálogo de garantías.
     */
    private String descripcionGarantiaCredito;

    /**
     * Tipo de garantía del catálogo.
     * Se conserva como dato informativo.
     */
    private String tipoGarantia;
}