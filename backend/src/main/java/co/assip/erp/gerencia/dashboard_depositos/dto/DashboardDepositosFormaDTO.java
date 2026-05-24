package co.assip.erp.gerencia.dashboard_depositos.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardDepositosFormaDTO {

    private Integer idFormaAhorro;

    private String codigoForma;
    private String nombreForma;

    private Integer cuentasActuales;
    private BigDecimal saldoActual;

    private Integer hombres;
    private Integer mujeres;
    private Integer juridicas;

    private Integer cuentasAnteriores;
    private BigDecimal saldoAnterior;

    private BigDecimal ingresosPeriodo;
    private BigDecimal egresosPeriodo;

    private BigDecimal variacionSaldo;
    private Integer variacionCuentas;

    private BigDecimal porcentajeCrecimiento;

}