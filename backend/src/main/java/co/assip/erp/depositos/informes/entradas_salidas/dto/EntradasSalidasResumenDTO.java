package co.assip.erp.depositos.informes.entradas_salidas.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EntradasSalidasResumenDTO {

    private Integer totalDias;
    private Integer totalMovimientos;

    private BigDecimal totalEntradas;
    private BigDecimal totalSalidas;
    private BigDecimal totalNeto;

}