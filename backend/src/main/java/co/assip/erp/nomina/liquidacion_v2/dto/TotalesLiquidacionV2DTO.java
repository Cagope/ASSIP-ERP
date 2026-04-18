package co.assip.erp.nomina.liquidacion_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotalesLiquidacionV2DTO {

    /**
     * Total devengados del contrato
     */
    private BigDecimal totalDevengados;

    /**
     * Total deducciones del contrato
     */
    private BigDecimal totalDeducciones;

    /**
     * Total provisiones del contrato
     */
    private BigDecimal totalProvisiones;

    /**
     * Neto a pagar
     */
    private BigDecimal netoPagar;

    /**
     * Ingreso base de cotización
     */
    private BigDecimal ibc;

    /**
     * Días liquidados (resultado del motor de tiempo)
     */
    private BigDecimal diasLaborados;
}