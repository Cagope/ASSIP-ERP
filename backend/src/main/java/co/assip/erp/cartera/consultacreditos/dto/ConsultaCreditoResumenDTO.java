package co.assip.erp.cartera.consultacreditos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Información resumida utilizada en la grilla de
 * consulta de créditos del asociado.
 *
 * Fuente:
 * cartera.vw_cartera_creditos_total
 */
@Getter
@Setter
public class ConsultaCreditoResumenDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCarteraCredito;

    private String pagareCartera;

    // =========================================================
    // Línea de crédito
    // =========================================================

    private Integer idLineaCredito;

    private String codigoLineaCredito;

    private String descripcionLineaCredito;

    // =========================================================
    // Estado
    // =========================================================

    private String codigoEstadoCartera;

    private String descripcionEstadoCartera;

    private String codigoEstadoJuridico;

    private String descripcionEstadoJuridico;

    // =========================================================
    // Fechas
    // =========================================================

    private LocalDate fechaDesembolso;

    private LocalDate fechaFinal;

    // =========================================================
    // Valores
    // =========================================================

    private BigDecimal valorDesembolsado;

    private BigDecimal saldoActual;

    // =========================================================
    // Riesgo
    // =========================================================

    private String edadDeRiesgo;

    private String descripcionEdadDeRiesgo;

    private String edadDeMora;

    private String descripcionEdadDeMora;

    // =========================================================
    // Indicadores
    // =========================================================

    private Boolean creditoSaldado;

    private Boolean creditoEnMora;

    private Boolean riesgoAlto;

    private Boolean requiereRevision;

}