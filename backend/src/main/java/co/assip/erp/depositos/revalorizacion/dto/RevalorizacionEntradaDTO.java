package co.assip.erp.depositos.revalorizacion.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RevalorizacionEntradaDTO {

    private Integer agenciaId;
    private Integer formaId;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private LocalDate fechaProceso;
    private LocalDate fechaLiquidacion;

    private BigDecimal tasaRevalorizacion;

    // 🔵 COMPROBANTE
    private String tipoComprobante;
    private String numeroComprobante;

}