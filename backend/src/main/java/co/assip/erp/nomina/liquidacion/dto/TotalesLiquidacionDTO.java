package co.assip.erp.nomina.liquidacion.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de totales de una liquidación de nómina.
 * Se usa exclusivamente para validaciones post-liquidación.
 */
@Getter
@Setter
public class TotalesLiquidacionDTO {

    private BigDecimal totalDevengados;
    private BigDecimal totalDeducciones;
    private BigDecimal totalProvisiones;
    private BigDecimal netoPagar;

}
