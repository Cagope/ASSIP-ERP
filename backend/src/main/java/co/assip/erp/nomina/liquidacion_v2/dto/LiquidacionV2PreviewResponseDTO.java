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
public class LiquidacionV2PreviewResponseDTO {

    private Integer idPeriodoNomina;
    private Integer fkAgencia;

    private Integer totalContratos;

    private BigDecimal totalDevengados;
    private BigDecimal totalDeducciones;
    private BigDecimal totalProvisiones;
    private BigDecimal totalNetoPagar;

    private List<LiquidacionV2PreviewItemDTO> items;
}