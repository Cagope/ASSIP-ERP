package co.assip.erp.depositos.informes.extracto_por_valor.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ExtractoPorValorResumenDTO {

    private Integer totalMovimientos;

    private BigDecimal totalDebitos;
    private BigDecimal totalCreditos;
    private BigDecimal totalNeto;

}