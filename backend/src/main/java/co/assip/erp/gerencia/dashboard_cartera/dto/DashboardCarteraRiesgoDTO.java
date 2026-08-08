package co.assip.erp.gerencia.dashboard_cartera.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DashboardCarteraRiesgoDTO {

    // =========================================================
    // Clasificación
    // =========================================================
    private String codigo;
    private String descripcion;
    private Integer orden;

    // =========================================================
    // Indicadores
    // =========================================================
    private Long cantidadCreditos;
    private Long cantidadAsociados;

    private BigDecimal saldoCartera;
    private BigDecimal saldoCreditosMora;
    private BigDecimal porcentajeParticipacion;
}