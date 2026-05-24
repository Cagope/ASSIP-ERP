package co.assip.erp.gerencia.dashboard_depositos.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardDepositosResumenDTO {

    private Integer totalCuentas;

    private BigDecimal saldoTotal;

    private BigDecimal totalDebitos;

    private BigDecimal totalCreditos;

    private Integer totalFormas;

    private Integer hombres;

    private Integer mujeres;

    private Integer juridicas;

    private BigDecimal saldoAportes;

    private BigDecimal saldoTac;

    private Integer totalAsociados;

}