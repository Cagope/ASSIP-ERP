package co.assip.erp.gerencia.dashboard_depositos.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardDepositosTipoCaptacionDTO {

    private String codigoCaptacion;
    private String descripcionCaptacion;

    private Integer cuentasAnteriores;
    private BigDecimal saldoAnterior;

    private Integer cuentasActuales;
    private BigDecimal saldoActual;

    private BigDecimal ingresosPeriodo;
    private BigDecimal egresosPeriodo;

    private BigDecimal variacionSaldo;
    private Integer variacionCuentas;
    private BigDecimal porcentajeCrecimiento;

    private BigDecimal participacion;
}