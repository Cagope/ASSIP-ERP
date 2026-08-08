package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio403.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Criterio403DatoDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCierreCarteraCredito;

    private Integer idCarteraCredito;

    private Integer idDatosPersonal;

    private String documento;

    // =========================================================
    // Criterio 403 - Capacidad de pago
    // =========================================================

    /**
     * Total de ingresos del asociado almacenado en la
     * fotografía histórica de Hoja de Vida del corte.
     */
    private BigDecimal ingresosTotales;

    /**
     * Total de egresos del asociado almacenado en la
     * fotografía histórica de Hoja de Vida del corte.
     */
    private BigDecimal egresosTotales;
}