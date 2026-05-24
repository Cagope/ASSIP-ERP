package co.assip.erp.gerencia.dashboard_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardCdatResumenDTO {

    private Integer totalCdats;

    private BigDecimal valorTotalCaptado;

    private BigDecimal promedioTasa;

    private BigDecimal promedioPlazo;

    private Integer vencen30Dias;

    private Integer renovacionesMes;

    private Integer cancelacionesMes;

    private Integer totalAsociados;

}