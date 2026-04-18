package co.assip.erp.nomina.liquidacion_v2.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiquidacionV2PeriodoDisponibleDTO {

    private Integer idPeriodo;
    private Integer anio;
    private Integer mes;
    private Integer numeroPeriodo;
    private String tipoPeriodo;
}