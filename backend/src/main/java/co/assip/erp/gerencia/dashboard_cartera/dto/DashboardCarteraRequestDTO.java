package co.assip.erp.gerencia.dashboard_cartera.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DashboardCarteraRequestDTO {

    // =========================================================
    // Fechas
    // =========================================================
    private LocalDate fechaCorte;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;

    // =========================================================
    // Agencia y línea de crédito
    // =========================================================
    private Long idAgencia;
    private Long idLineaCredito;

    // =========================================================
    // Riesgo y mora
    // =========================================================
    private String edadRiesgo;
    private String edadMora;

    // =========================================================
    // Estados
    // =========================================================
    private String codigoEstadoCartera;
    private String codigoEstadoJuridico;

    // =========================================================
    // Clasificación y garantía
    // =========================================================
    private String codigoClasificacionCredito;
    private String codigoGarantiaCredito;
}