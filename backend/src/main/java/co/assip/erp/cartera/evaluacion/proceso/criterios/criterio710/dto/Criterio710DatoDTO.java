package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio710.dto;

import lombok.Data;

@Data
public class Criterio710DatoDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCierreCarteraCredito;

    private Integer idCarteraCredito;

    private Integer idDatosPersonal;

    private String documento;

    // =========================================================
    // Criterio 710 - Sector económico
    // =========================================================

    /**
     * Código de sector económico almacenado en la fotografía
     * histórica de Hoja de Vida.
     *
     * Valores esperados según la parametrización actual:
     *
     * 1  a 24
     * 99 = Otras actividades
     *
     * El código se convierte a valor técnico y se envía al
     * selector genérico de reglas.
     */
    private String codigoSectorEconomico;
}