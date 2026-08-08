package co.assip.erp.gerencia.dashboard_cartera.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DashboardCarteraRecaudoDTO {

    // =========================================================
    // Concepto de recaudo
    // =========================================================
    private String codigo;
    private String descripcion;
    private Integer orden;

    // =========================================================
    // Indicadores
    // =========================================================
    private Long cantidadMovimientos;

    private BigDecimal valorRecaudado;
    private BigDecimal porcentajeParticipacion;
}