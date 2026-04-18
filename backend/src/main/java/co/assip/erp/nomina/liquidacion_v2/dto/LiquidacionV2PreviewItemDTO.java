package co.assip.erp.nomina.liquidacion_v2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiquidacionV2PreviewItemDTO {

    private Integer idContrato;
    private Integer idEmpleado;

    private String documentoEmpleado;
    private String nombreEmpleado;

    private BigDecimal salarioBase;
    private BigDecimal diasLaborados;
    private BigDecimal ibc;

    private BigDecimal totalDevengados;
    private BigDecimal totalDeducciones;
    private BigDecimal totalProvisiones;
    private BigDecimal netoPagar;

    private List<LiquidacionV2DetalleDTO> detalle;
}