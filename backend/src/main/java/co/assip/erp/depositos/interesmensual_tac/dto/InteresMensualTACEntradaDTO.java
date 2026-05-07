package co.assip.erp.depositos.interesmensual_tac.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InteresMensualTACEntradaDTO {

    private Integer agenciaId;
    private Integer formaId;

    private LocalDate fechaProceso;
    private LocalDate fechaLiquidacion;

    // 🔵 COMPROBANTE
    private String tipoComprobante;
    private String numeroComprobante;

}