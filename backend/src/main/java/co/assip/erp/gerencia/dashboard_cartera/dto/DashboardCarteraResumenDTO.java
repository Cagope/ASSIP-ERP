package co.assip.erp.gerencia.dashboard_cartera.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DashboardCarteraResumenDTO {

    // =========================================================
    // Posición actual de cartera
    // =========================================================
    private BigDecimal saldoCartera;
    private BigDecimal saldoNetoPendiente;
    private BigDecimal saldoCreditosMora;
    private BigDecimal indiceMora;

    // =========================================================
    // Cantidades principales
    // =========================================================
    private Long cantidadCreditosConSaldo;
    private Long cantidadAsociados;
    private Long cantidadCreditosMora;
    private Long cantidadCreditosCriticos;

    // =========================================================
    // Recaudos del período
    // =========================================================
    private BigDecimal recaudoPeriodo;
    private BigDecimal capitalRecaudado;
    private BigDecimal interesesRecaudados;
    private BigDecimal interesesMoraRecaudados;

    // =========================================================
    // Evolución y situación especial
    // =========================================================
    private Long cantidadDeteriorados;
    private Long cantidadMejorados;
    private Long cantidadReestructurados;
    private Long cantidadJuridicos;
}