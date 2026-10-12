package co.assip.erp.cajas.cierre.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CajaCierreMovimientosDTO {

    private BigDecimal totalIngresos;
    private BigDecimal totalEgresos;

    private BigDecimal entradasEfectivo;
    private BigDecimal salidasEfectivo;

    private BigDecimal entradasCheques;
    private BigDecimal salidasCheques;

    private Integer cantidadMovimientos;
}
