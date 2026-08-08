package co.assip.erp.gerencia.dashboard_cartera.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DashboardCarteraAgenciaDTO {

    // =========================================================
    // Agencia
    // =========================================================
    private Long idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

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
    // Recaudos
    // =========================================================
    private BigDecimal recaudoPeriodo;

    // =========================================================
    // Indicadores porcentuales
    // =========================================================
    private BigDecimal indiceMora;
    private BigDecimal porcentajeParticipacion;
}