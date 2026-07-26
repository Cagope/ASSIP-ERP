package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ExpedienteIndicadoresDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Long idDatosPersonal;

    private String documento;
    private String nombreCompleto;

    // =========================================================
    // Productos
    // =========================================================

    private Integer cantidadCuentasAhorro;
    private Integer cantidadCdats;
    private Integer cantidadCreditos;

    private Integer cantidadBienes;

    private Integer cantidadGarantias;

    // =========================================================
    // Patrimonio
    // =========================================================

    private BigDecimal totalAportes;

    private BigDecimal totalAhorros;

    private BigDecimal totalCdats;

    private BigDecimal totalBienes;

    private BigDecimal totalPatrimonio;

    private BigDecimal totalObligaciones;

    private BigDecimal patrimonioNeto;

    // =========================================================
    // Cartera
    // =========================================================

    private BigDecimal saldoCapital;

    private BigDecimal saldoTotalCredito;

    private Integer creditosEnMora;

    private Integer diasMayorMora;

    private String mayorEdadRiesgo;

    private BigDecimal provisionTotal;

    // =========================================================
    // Garantías
    // =========================================================

    private BigDecimal valorGarantias;

    private BigDecimal coberturaGarantias;

    private Boolean garantiasSuficientes;

    // =========================================================
    // Riesgo
    // =========================================================

    private String nivelRiesgo;

    private String colorRiesgo;

    private Integer cantidadAlertasCriticas;

    private Integer cantidadAlertasAdvertencia;

    private Integer cantidadAlertasInformativas;

    // =========================================================
    // Cumplimiento
    // =========================================================

    private Boolean sarlaftVigente;

    private Boolean contactoCompleto;

    private Boolean informacionFinancieraCompleta;

    private Boolean documentacionCompleta;

    // =========================================================
    // Indicadores porcentuales
    // =========================================================

    private BigDecimal porcentajeCoberturaPatrimonial;

    private BigDecimal porcentajeReciprocidad;

    private BigDecimal porcentajeEndeudamiento;

    private BigDecimal porcentajeCompromisoIngresos;

    // =========================================================
    // Estado general
    // =========================================================

    private String estadoGeneral;

    private String resumenEjecutivo;

    private LocalDate fechaActualizacion;
}