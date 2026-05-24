package co.assip.erp.cdat.informes.fechas_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FechasCdatResumenDTO {

    private Integer cantidad;

    private BigDecimal valorTotal;

    private BigDecimal promedioTasa;

    private BigDecimal promedioPlazo;

}