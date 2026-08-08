package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio502.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Criterio502DatoDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCierreCarteraCredito;

    private Integer idCarteraCredito;

    private Integer idDatosPersonal;

    private String documento;

    // =========================================================
    // Criterio 502 - Solvencia del deudor
    // =========================================================

    /**
     * Total de activos almacenado en la fotografía histórica
     * de Hoja de Vida correspondiente a la fecha de corte.
     */
    private BigDecimal totalActivos;

    /**
     * Total de pasivos almacenado en la fotografía histórica
     * de Hoja de Vida correspondiente a la fecha de corte.
     */
    private BigDecimal totalPasivos;
}