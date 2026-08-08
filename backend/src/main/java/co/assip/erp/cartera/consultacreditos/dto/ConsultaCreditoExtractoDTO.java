package co.assip.erp.cartera.consultacreditos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Movimiento del extracto de un crédito.
 *
 * Fuente:
 * cartera.vw_cartera_extractos_total
 *
 * El DTO expone los valores entregados por la vista:
 *
 * - Aplicación del pago por concepto.
 * - Medios de pago utilizados.
 * - Fechas del movimiento.
 * - Información de mora.
 * - Acumulados del crédito.
 * - Controles de consistencia.
 *
 * No realiza cálculos adicionales.
 */
@Getter
@Setter
public class ConsultaCreditoExtractoDTO {

    // =========================================================
    // Identificación del movimiento
    // =========================================================

    private Integer idExtractoCartera;
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
    // Línea de crédito
    // =========================================================

    private Integer idLineaCredito;
    private String codigoLineaCredito;
    private String nombreLineaCredito;

    // =========================================================
    // Estado del crédito
    // =========================================================

    private String codigoEstadoCartera;
    private String descripcionEstadoCartera;

    private String codigoEstadoJuridico;
    private String descripcionEstadoJuridico;

    // =========================================================
    // Clasificación
    // =========================================================

    private String codigoClasificacionCredito;
    private String descripcionClasificacionCredito;

    // =========================================================
    // Garantía
    // =========================================================

    private String codigoGarantiaCredito;
    private String descripcionGarantiaCredito;
    private String tipoGarantia;

    // =========================================================
    // Riesgo y mora del crédito
    // =========================================================

    private String edadDeRiesgo;
    private String descripcionEdadDeRiesgo;

    private String edadDeMora;
    private String descripcionEdadDeMora;

    // =========================================================
    // Valores generales del crédito
    // =========================================================

    private BigDecimal valorInicialCredito;
    private BigDecimal valorDesembolsado;
    private BigDecimal valorCuota;
    private BigDecimal saldoActualCredito;

    // =========================================================
    // Fecha y comprobante del movimiento
    // =========================================================

    private LocalDate fechaPago;
    private LocalDateTime hora;
    private LocalDate fechaContable;

    private Integer diasDesdePago;

    private String tipoComprobante;
    private String numeroComprobante;
    private String comprobanteCompleto;

    // =========================================================
    // Aplicación del pago por concepto
    // =========================================================

    private BigDecimal valorCapital;

    private BigDecimal valorInteresCausado;
    private BigDecimal valorInteresIngreso;
    private BigDecimal valorInteresAnticipado;
    private BigDecimal valorInteresMora;

    private BigDecimal valorSeguro;
    private BigDecimal valorAportes;
    private BigDecimal valorPapeleria;
    private BigDecimal valorFondoGarantia;
    private BigDecimal otrosValores;

    // =========================================================
    // Totales del movimiento
    // =========================================================

    private BigDecimal totalInteresesRegistrados;
    private BigDecimal totalOtrosConceptos;
    private BigDecimal totalComponentesRegistrados;

    // =========================================================
    // Medios de pago
    // =========================================================

    private BigDecimal pagoEfectivo;
    private BigDecimal pagoCheques;
    private BigDecimal pagoNotas;

    private Integer idCuentaAhorro;
    private String codigoCuentaAhorro;
    private String nombreFormaAhorro;
    private BigDecimal saldoActualCuentaAhorro;

    private BigDecimal pagoConsignacion;
    private BigDecimal pagoPse;
    private BigDecimal otroPago;

    private BigDecimal totalMediosPago;
    private BigDecimal diferenciaMediosPagoComponentes;

    // =========================================================
    // Control de medios de pago
    // =========================================================

    private Boolean movimientoCuadrado;

    private Boolean usoEfectivo;
    private Boolean usoCheques;
    private Boolean usoNotas;
    private Boolean usoCuentaAhorro;
    private Boolean usoConsignacion;
    private Boolean usoPse;
    private Boolean usoOtroPago;

    private Integer cantidadMediosPagoUtilizados;
    private String medioPagoPrincipal;

    // =========================================================
    // Observaciones
    // =========================================================

    private String observacionPago;
    private Boolean tieneObservacion;

    // =========================================================
    // Información de días y mora
    // =========================================================

    private Integer diasCapitalIngreso;
    private Integer diasInteresIngreso;
    private Integer diasMora;

    private Boolean pagoConMora;

    private String rangoDiasMora;
    private Integer ordenRangoMora;

    // =========================================================
    // Estado de ejecución del crédito
    // =========================================================

    private Integer alturaCuota;
    private BigDecimal abonosPendientes;

    // =========================================================
    // Últimas fechas por concepto
    // =========================================================

    private LocalDate ultimaFechaCapital;
    private LocalDate ultimaFechaInteres;
    private LocalDate ultimaFechaSeguro;
    private LocalDate ultimaFechaMora;

    // =========================================================
    // Próximas fechas por concepto
    // =========================================================

    private LocalDate proximaFechaCapital;
    private LocalDate proximaFechaInteres;
    private LocalDate proximaFechaSeguro;

    private LocalDate interesesPagadosHasta;

    // =========================================================
    // Tarjeta y módulo de origen
    // =========================================================

    private String tarjeta;
    private String modulo;

    private Boolean movimientoConTarjeta;

    // =========================================================
    // Estado del movimiento
    // =========================================================

    private String estado;
    private Boolean movimientoActivo;

    // =========================================================
    // Conceptos aplicados
    // =========================================================

    private Boolean aplicoCapital;
    private Boolean aplicoIntereses;
    private Boolean aplicoInteresMora;
    private Boolean aplicoSeguro;
    private Boolean aplicoAportes;
    private Boolean aplicoFondoGarantia;
    private Boolean aplicoPapeleria;

    // =========================================================
    // Indicadores del pago
    // =========================================================

    private BigDecimal porcentajePagoAplicadoCapital;

    // =========================================================
    // Secuencia y acumulados
    // =========================================================

    private Long numeroMovimientoCredito;
    private Long cantidadMovimientosCredito;

    private BigDecimal capitalAcumuladoHastaMovimiento;
    private BigDecimal recaudoAcumuladoHastaMovimiento;

    private LocalDate fechaUltimoMovimientoCredito;

    private Boolean perteneceUltimaFechaMovimiento;

    // =========================================================
    // Auditoría
    // =========================================================

    private Integer fkSeguridadCreacion;
    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;
    private LocalDateTime fechaEdicion;
}