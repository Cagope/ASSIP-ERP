package co.assip.erp.cartera.consultacreditos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Evaluación de cartera de un crédito.
 *
 * Fuente:
 * cartera.vw_cartera_evaluaciones_total
 *
 * Representa el historial de evaluaciones realizadas
 * al crédito y el resultado obtenido en cada una.
 *
 * No realiza cálculos.
 */
@Getter
@Setter
public class ConsultaCreditoEvaluacionDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idEvaluacionCartera;
    private Integer idCarteraCredito;

    private String pagareCartera;

    private Integer idDatosPersonal;

    // =========================================================
    // Agencia
    // =========================================================

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    // =========================================================
    // Crédito
    // =========================================================

    private Integer idLineaCredito;

    private String codigoLineaCredito;
    private String nombreLineaCredito;

    private String codigoEstadoCartera;
    private String descripcionEstadoCartera;

    private String codigoEstadoJuridico;
    private String descripcionEstadoJuridico;

    // =========================================================
    // Evaluación
    // =========================================================

    private Integer numeroEvaluacion;

    private LocalDate fechaEvaluacion;
    private LocalDate fechaContable;

    private Boolean ultimaEvaluacionCredito;

    // =========================================================
    // Riesgo
    // =========================================================

    private String edadRiesgoAnterior;
    private String descripcionEdadRiesgoAnterior;

    private String edadRiesgoInicial;
    private String descripcionEdadRiesgoInicial;

    private String edadMora;
    private String descripcionEdadMora;

    private String edadRiesgoEvaluada;
    private String descripcionEdadRiesgoEvaluada;

    private String edadRiesgoNueva;
    private String descripcionEdadRiesgoNueva;

    // =========================================================
    // Variación del riesgo
    // =========================================================

    private String resultadoEvaluacion;

    private String accionEvaluacion;

    private String variacionRiesgo;

    private Boolean riesgoMejora;
    private Boolean riesgoIgual;
    private Boolean riesgoDeteriora;

    // =========================================================
    // Indicadores
    // =========================================================

    private Integer ordenEdadRiesgoAnterior;
    private Integer ordenEdadRiesgoNueva;

    private Integer diferenciaNivelRiesgo;

    // =========================================================
    // Valores del crédito al momento de evaluar
    // =========================================================

    private BigDecimal saldoCapital;

    private BigDecimal saldoInteresCorriente;
    private BigDecimal saldoInteresMora;

    private BigDecimal saldoSeguro;
    private BigDecimal saldoOtrosConceptos;

    private BigDecimal saldoTotal;

    // =========================================================
    // Estado
    // =========================================================

    private Boolean evaluacionAplicada;
    private Boolean requiereRevision;

    // =========================================================
    // Observaciones
    // =========================================================

    private String comentarioEvaluacion;
    private String observacion;

    // =========================================================
    // Alertas
    // =========================================================

    private String nivelAlertaEvaluacion;

    private Integer ordenAlertaEvaluacion;

    private String motivoAlertaEvaluacion;

    // =========================================================
    // Responsable
    // =========================================================

    private Integer idUsuarioEvaluacion;

    private String usuarioEvaluacion;

    // =========================================================
    // Auditoría
    // =========================================================

    private Integer fkSeguridadCreacion;
    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;
    private LocalDateTime fechaEdicion;

}