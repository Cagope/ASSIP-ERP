package co.assip.erp.cartera.consultacreditos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Información de alivios aplicados al crédito.
 *
 * Fuente:
 * cartera.vw_cartera_alivios_total
 *
 * Representa tanto el encabezado del alivio como
 * el detalle financiero generado por cada aplicación.
 */
@Getter
@Setter
public class ConsultaCreditoAlivioDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCreditoAlivio;
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
    // Alivio
    // =========================================================

    private Integer idTipoAlivio;
    private String codigoTipoAlivio;
    private String descripcionTipoAlivio;

    private String numeroActa;
    private String numeroResolucion;

    private LocalDate fechaAlivio;
    private LocalDate fechaAplicacion;

    // =========================================================
    // Período del alivio
    // =========================================================

    private LocalDate periodoInicial;
    private LocalDate periodoFinal;

    private Integer cantidadPeriodos;

    // =========================================================
    // Valores del alivio
    // =========================================================

    private BigDecimal valorCapital;

    private BigDecimal valorInteresCorriente;

    private BigDecimal valorInteresMora;

    private BigDecimal valorSeguro;

    private BigDecimal valorFondoGarantia;

    private BigDecimal valorOtrosConceptos;

    private BigDecimal valorTotalAlivio;

    // =========================================================
    // Valores aplicados
    // =========================================================

    private BigDecimal valorCapitalAplicado;

    private BigDecimal valorInteresAplicado;

    private BigDecimal valorMoraAplicada;

    private BigDecimal valorSeguroAplicado;

    private BigDecimal valorOtrosAplicados;

    private BigDecimal valorTotalAplicado;

    // =========================================================
    // Saldos
    // =========================================================

    private BigDecimal saldoCapital;

    private BigDecimal saldoInteres;

    private BigDecimal saldoMora;

    private BigDecimal saldoSeguro;

    private BigDecimal saldoOtrosConceptos;

    private BigDecimal saldoPendiente;

    // =========================================================
    // Estado
    // =========================================================

    private Boolean alivioActivo;

    private Boolean alivioFinalizado;

    private Boolean alivioAnulado;

    private Boolean requiereRevision;

    // =========================================================
    // Indicadores
    // =========================================================

    private Integer cuotasBeneficiadas;

    private Integer cuotasPendientes;

    private BigDecimal porcentajeAplicado;

    private BigDecimal porcentajePendiente;

    // =========================================================
    // Observaciones
    // =========================================================

    private String observacion;

    private String comentario;

    // =========================================================
    // Alertas
    // =========================================================

    private String nivelAlertaAlivio;

    private Integer ordenAlertaAlivio;

    private String motivoAlertaAlivio;

    // =========================================================
    // Auditoría
    // =========================================================

    private Integer fkSeguridadCreacion;

    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;

    private LocalDateTime fechaEdicion;

}