package co.assip.erp.gerencia.dashboard_cartera.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DashboardCarteraResponseDTO {

    // =========================================================
    // Fechas del Dashboard
    // =========================================================
    private LocalDate fechaCorte;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;

    // =========================================================
    // Resumen Ejecutivo
    // =========================================================
    private DashboardCarteraResumenDTO resumen =
            new DashboardCarteraResumenDTO();

    // =========================================================
    // Distribución por Edad de Riesgo
    // =========================================================
    private List<DashboardCarteraRiesgoDTO> riesgos =
            new ArrayList<>();

    // =========================================================
    // Distribución por Edad de Mora
    // =========================================================
    private List<DashboardCarteraRiesgoDTO> moras =
            new ArrayList<>();

    // =========================================================
    // Distribución por Línea de Crédito
    // =========================================================
    private List<DashboardCarteraLineaDTO> lineas =
            new ArrayList<>();

    // =========================================================
    // Distribución por Agencia
    // =========================================================
    private List<DashboardCarteraAgenciaDTO> agencias =
            new ArrayList<>();

    // =========================================================
    // Composición del Recaudo
    // =========================================================
    private List<DashboardCarteraRecaudoDTO> recaudos =
            new ArrayList<>();

    // =========================================================
    // Alertas Gerenciales
    // =========================================================
    private List<DashboardCarteraAlertaDTO> alertas =
            new ArrayList<>();
}