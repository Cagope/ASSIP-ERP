package co.assip.erp.depositos.analisis.dashboard_depositos.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardDepositosGrupoDTO {

    private String concepto;

    private Integer cuentasAportes;
    private BigDecimal valorAportes;
    private BigDecimal participacionAportes;

    private Integer cuentasAhorros;
    private BigDecimal valorAhorros;
    private BigDecimal participacionAhorros;

    private Integer ranking;

}