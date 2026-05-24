package co.assip.erp.gerencia.dashboard_depositos.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardDepositosTendenciaDTO {

    private String periodo;

    private Integer totalCuentasAportes;
    private Integer totalCuentasDepositos;

    private BigDecimal saldoAportes;
    private BigDecimal saldoDepositos;

}