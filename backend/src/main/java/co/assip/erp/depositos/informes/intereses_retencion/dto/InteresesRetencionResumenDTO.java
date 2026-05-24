package co.assip.erp.depositos.informes.intereses_retencion.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class InteresesRetencionResumenDTO {

    private Integer totalCuentas;

    private BigDecimal totalIntereses;
    private BigDecimal totalRetencion;
    private BigDecimal totalNeto;

}