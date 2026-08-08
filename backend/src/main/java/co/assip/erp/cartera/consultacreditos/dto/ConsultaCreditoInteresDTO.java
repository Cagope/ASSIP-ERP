package co.assip.erp.cartera.consultacreditos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Causación de intereses del crédito.
 *
 * Fuente:
 * cartera.vw_cartera_intereses_causados_total
 *
 * Representa cada proceso de causación registrado
 * para un crédito.
 */
@Getter
@Setter
public class ConsultaCreditoInteresDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idInteresCausado;
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
    // Período de causación
    // =========================================================

    private LocalDate fechaProceso;

    private LocalDate periodoInicial;
    private LocalDate periodoFinal;

    private Integer diasLiquidados;

    // =========================================================
    // Base de liquidación
    // =========================================================

    private BigDecimal saldoCapital;

    private BigDecimal saldoNetoPendiente;

    private BigDecimal saldoBaseLiquidacion;

    // =========================================================
    // Tasas
    // =========================================================

    private BigDecimal tasaNominal;

    private BigDecimal tasaEfectiva;

    private BigDecimal tasaMora;

    // =========================================================
    // Valores causados
    // =========================================================

    private BigDecimal interesCorrienteCausado;

    private BigDecimal interesMoraCausado;

    private BigDecimal seguroCausado;

    private BigDecimal fondoGarantiaCausado;

    private BigDecimal otrosConceptosCausados;

    private BigDecimal totalCausado;

    // =========================================================
    // Valores pagados
    // =========================================================

    private BigDecimal interesCorrientePagado;

    private BigDecimal interesMoraPagado;

    private BigDecimal seguroPagado;

    private BigDecimal fondoGarantiaPagado;

    private BigDecimal otrosConceptosPagados;

    private BigDecimal totalPagado;

    // =========================================================
    // Saldos
    // =========================================================

    private BigDecimal saldoInteresCorriente;

    private BigDecimal saldoInteresMora;

    private BigDecimal saldoSeguro;

    private BigDecimal saldoFondoGarantia;

    private BigDecimal saldoOtrosConceptos;

    private BigDecimal saldoPendiente;

    // =========================================================
    // Riesgo
    // =========================================================

    private String edadDeRiesgo;

    private String descripcionEdadDeRiesgo;

    private String edadDeMora;

    private String descripcionEdadDeMora;

    // =========================================================
    // Estado
    // =========================================================

    private Boolean procesoActivo;

    private Boolean procesoAplicado;

    private Boolean requiereRevision;

    // =========================================================
    // Indicadores
    // =========================================================

    private BigDecimal porcentajeCobrado;

    private BigDecimal porcentajePendiente;

    private Integer numeroProceso;

    // =========================================================
    // Observaciones
    // =========================================================

    private String observacion;

    private String comentario;

    // =========================================================
    // Alertas
    // =========================================================

    private String nivelAlerta;

    private Integer ordenAlerta;

    private String motivoAlerta;

    // =========================================================
    // Auditoría
    // =========================================================

    private Integer fkSeguridadCreacion;

    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;

    private LocalDateTime fechaEdicion;

}