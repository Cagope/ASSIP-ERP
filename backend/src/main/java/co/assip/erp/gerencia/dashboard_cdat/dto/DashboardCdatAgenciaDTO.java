package co.assip.erp.gerencia.dashboard_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardCdatAgenciaDTO {

    private String agencia;

    private Integer cantidad;

    private BigDecimal valorTotal;

    private BigDecimal promedioTasa;

    private BigDecimal participacion;

}