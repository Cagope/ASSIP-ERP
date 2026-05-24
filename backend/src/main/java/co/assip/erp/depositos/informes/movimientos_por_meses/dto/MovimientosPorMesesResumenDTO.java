package co.assip.erp.depositos.informes.movimientos_por_meses.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MovimientosPorMesesResumenDTO {

    private String mes;

    private Integer cantidadMovimientos;

    private BigDecimal entradas;

    private BigDecimal salidas;

}