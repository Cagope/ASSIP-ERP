package co.assip.erp.gerencia.dashboard_cartera.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DashboardCarteraLineaDTO {

    // =========================================================
    // Línea de crédito
    // =========================================================
    private Long idLineaCredito;
    private String codigoLineaCredito;
    private String nombreLineaCredito;

    // =========================================================
    // Indicadores
    // =========================================================
    private Long cantidadCreditos;
    private Long cantidadAsociados;

    private BigDecimal valorDesembolsado;
    private BigDecimal saldoCartera;
    private BigDecimal saldoNetoPendiente;
    private BigDecimal saldoCreditosMora;

    // =========================================================
    // Indicadores porcentuales
    // =========================================================
    private BigDecimal indiceMora;
    private BigDecimal porcentajeParticipacion;
}