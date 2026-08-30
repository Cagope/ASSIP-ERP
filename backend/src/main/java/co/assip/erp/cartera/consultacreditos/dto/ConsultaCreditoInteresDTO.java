package co.assip.erp.cartera.consultacreditos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Movimiento de intereses causados del crédito.
 *
 * Fuente:
 * cartera.vw_cartera_intereses_causados_total
 *
 * Representa cada movimiento registrado:
 *
 * - causación;
 * - pago;
 * - ajuste.
 *
 * El saldo se controla mediante:
 *
 * valor_debito - valor_credito
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
    // Movimiento
    // =========================================================

    private LocalDate fechaMovimiento;

    private LocalDate fechaInicialPeriodo;
    private LocalDate fechaFinalPeriodo;

    private Integer diasCausados;

    // =========================================================
    // Base de liquidación
    // =========================================================

    private BigDecimal saldoBase;

    private BigDecimal interesBruto;

    private BigDecimal porcentajeAplicacion;

    // =========================================================
    // Tasa
    // =========================================================

    private BigDecimal tasaInteres;

    // =========================================================
    // Débito / Crédito
    // =========================================================

    private BigDecimal valorDebito;

    private BigDecimal valorCredito;

    private BigDecimal valorMovimiento;

    private String naturalezaMovimiento;

    // =========================================================
    // Saldo acumulado
    // =========================================================

    private BigDecimal saldoInteresesAcumulado;

    // =========================================================
    // Comprobante
    // =========================================================

    private String tipoComprobante;

    private String numeroComprobante;

    private String comprobanteCompleto;

    // =========================================================
    // Observación
    // =========================================================

    private String observacionInteres;

    // =========================================================
    // Control del período
    // =========================================================

    private Integer diasCalendarioPeriodo;

    private Boolean diasCausadosCoincidenPeriodo;

    // =========================================================
    // Secuencia
    // =========================================================

    private Long numeroMovimientoCredito;

    private Long cantidadMovimientosCredito;

    // =========================================================
    // Resumen del crédito
    // =========================================================

    private Long cantidadMovimientos;

    private Long cantidadDebitos;

    private Long cantidadCreditos;

    private BigDecimal totalDebitos;

    private BigDecimal totalCreditos;

    private BigDecimal saldoIntereses;

    private LocalDate primeraFechaMovimiento;

    private LocalDate ultimaFechaMovimiento;

    // =========================================================
    // Riesgo del crédito
    // =========================================================

    private String edadDeRiesgo;

    private String descripcionEdadDeRiesgo;

    private String edadDeMora;

    private String descripcionEdadDeMora;

    private String edadContable;

    private String descripcionEdadContable;

    // =========================================================
    // Estado financiero del crédito
    // =========================================================

    private BigDecimal saldoActual;

    private BigDecimal saldoNetoPendiente;

    private Boolean creditoEnMora;

    private Boolean riesgoAlto;

    private Boolean requiereRevision;

    // =========================================================
    // Alertas del crédito
    // =========================================================

    private String nivelAlertaCredito;

    private String motivoAlertaCredito;

    // =========================================================
    // COMPATIBILIDAD TEMPORAL CON FRONT ACTUAL
    // =========================================================

    private LocalDate fechaProceso;

    private LocalDate periodoInicial;

    private LocalDate periodoFinal;

    private Integer diasLiquidados;

    private BigDecimal saldoCapital;

    private BigDecimal saldoBaseLiquidacion;

    private BigDecimal tasaNominal;

    private BigDecimal saldoPendiente;

    private String observacion;

    // =========================================================
    // Auditoría
    // =========================================================

    private Integer fkSeguridadCreacion;

    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;

    private LocalDateTime fechaEdicion;
}