package co.assip.erp.depositos.interesdiario_sm.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InteresDiarioSMEntradaDTO {

    private Integer agenciaId;
    private Integer formaId;

    private LocalDate fechaProceso;
    private LocalDate fechaLiquidacion;

    private String tipoComprobante;
    private String numeroComprobante;

    /**
     * true  → usuario confirmó continuar
     * false → advertir diferencia de días
     */
    private Boolean confirmado = false;

}