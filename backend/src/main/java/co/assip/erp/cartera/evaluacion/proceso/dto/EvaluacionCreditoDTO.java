package co.assip.erp.cartera.evaluacion.proceso.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EvaluacionCreditoDTO {

    // =========================================================
    // Evaluación
    // =========================================================

    private Integer idEvaluacionCartera;

    private Integer idCierreCartera;

    private Integer idCierreCarteraCredito;

    private Integer idCarteraCredito;

    // =========================================================
    // Asociado
    // =========================================================

    private Integer idDatosPersonal;

    private String documento;

    private String nombres;

    private String primerApellido;

    private String segundoApellido;

    // =========================================================
    // Crédito
    // =========================================================

    private String pagareCartera;

    private String codigoClasificacionCredito;

    private BigDecimal saldoActual;

    // =========================================================
    // Riesgo
    // =========================================================

    private Boolean creditoEvaluado;

    private String edadMora;

    private String edadRiesgoAnterior;

    private String edadRiesgoInicial;

    // =========================================================
    // Criterio 401 - Servicio de la deuda
    // =========================================================

    private Integer cantidadPagosUltimoAnio;

    private Integer cantidadPagosCapitalUltimoAnio;

    private Integer cantidadPagosEvaluablesUltimoAnio;

    private BigDecimal sumaDiasMoraUltimoAnio;

    private BigDecimal promedioDiasMoraUltimoAnio;
}