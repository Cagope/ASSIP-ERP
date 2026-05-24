package co.assip.erp.depositos.informes.estadisticos_asociados.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EstadisticosAsociadosResumenDTO {

    private Integer totalAsociados;

    private BigDecimal totalAportes;

}