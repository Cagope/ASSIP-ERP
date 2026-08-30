package co.assip.erp.cartera.consultacreditos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuración de seguros asociados al crédito.
 *
 * Fuente configuración:
 * cartera.vw_cartera_seguros_total
 *
 * Fuente movimientos:
 * cartera.vw_cartera_seguros_movimientos_total
 *
 * El DTO representa:
 *
 * - configuración vigente e histórica del seguro;
 * - movimientos asociados a cada configuración.
 *
 * No realiza cálculos.
 */
@Getter
@Setter
public class ConsultaCreditoSeguroDTO {

    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCreditoSeguro;
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
    // Clasificación y garantía
    // =========================================================

    private String codigoClasificacionCredito;
    private String descripcionClasificacionCredito;

    private String codigoGarantiaCredito;
    private String descripcionGarantiaCredito;

    // =========================================================
    // Configuración del seguro
    // =========================================================

    private BigDecimal porcentajeSeguro;

    private String tipoCobroSeguro;
    private String descripcionTipoCobroSeguro;

    private Boolean seguroActivo;
    private String descripcionEstadoSeguro;

    // =========================================================
    // Valores estimados
    // =========================================================

    private BigDecimal valorSeguroSobreDesembolsoEstimado;
    private BigDecimal valorSeguroSobreSaldoEstimado;
    private BigDecimal valorSeguroSobreSaldoNetoEstimado;
    private BigDecimal valorSeguroSobreCuotaEstimado;

    // =========================================================
    // Validaciones
    // =========================================================

    private Boolean porcentajeSeguroValido;
    private Boolean porcentajeSeguroNegativo;
    private Boolean porcentajeSeguroSuperaCien;

    private Boolean tieneTipoCobroSeguro;
    private Boolean configuracionSeguroCompleta;

    // =========================================================
    // Resumen del crédito
    // =========================================================

    private Integer cantidadConfiguracionesSeguro;
    private Integer cantidadSegurosActivos;
    private Integer cantidadSegurosInactivos;
    private Integer cantidadSegurosActivosValidos;

    private BigDecimal porcentajeTotalSeguroActivo;

    private Boolean tieneSeguroActivo;
    private Boolean tieneSeguroActivoValido;
    private Boolean tienePorcentajeInconsistente;
    private Boolean tieneMultiplesSegurosActivos;
    private Boolean tieneHistorialConfiguracionesSeguro;

    // =========================================================
    // Fechas
    // =========================================================

    private LocalDate ultimaFechaSeguro;
    private LocalDate proximaFechaSeguro;

    private Integer diasParaProximoSeguro;

    private Boolean fechaSeguroVencida;
    private Integer diasSeguroVencido;

    // =========================================================
    // Estado de vencimiento
    // =========================================================

    private String estadoVencimientoSeguro;

    // =========================================================
    // Alertas
    // =========================================================

    private String nivelAlertaSeguro;
    private String motivoAlertaSeguro;
    private Integer ordenAlertaSeguro;

    // =========================================================
    // Historial de configuraciones
    // =========================================================

    private Integer numeroConfiguracionSeguro;
    private Integer cantidadRegistrosSeguroCredito;

    private Boolean configuracionSeguroPrincipal;

    // =========================================================
    // Movimientos del seguro
    // =========================================================

    private List<MovimientoSeguroDTO> movimientos =
            new ArrayList<>();

    // =========================================================
    // Riesgo del crédito
    // =========================================================

    private String edadDeRiesgo;
    private String descripcionEdadDeRiesgo;

    private String edadDeMora;
    private String descripcionEdadDeMora;

    // =========================================================
    // Estado financiero
    // =========================================================

    private BigDecimal saldoActual;
    private BigDecimal saldoNetoPendiente;

    private Boolean creditoSaldado;
    private Boolean requiereRevision;

    // =========================================================
    // Auditoría
    // =========================================================

    private Integer fkSeguridadCreacion;
    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;
    private LocalDateTime fechaEdicion;


    // =========================================================
    // MOVIMIENTO DEL SEGURO
    //
    // Se mantiene dentro del mismo DTO para no crear
    // otro archivo únicamente para el detalle.
    //
    // Fuente:
    // cartera.vw_cartera_seguros_movimientos_total
    // =========================================================

    @Getter
    @Setter
    public static class MovimientoSeguroDTO {

        // =====================================================
        // Identificación
        // =====================================================

        private Integer idCreditoSeguroDetalle;
        private Integer idCreditoSeguro;
        private Integer idCarteraCredito;

        // =====================================================
        // Fecha
        // =====================================================

        private LocalDate fechaMovimiento;

        // =====================================================
        // Base de liquidación
        // =====================================================

        private BigDecimal saldoBase;
        private BigDecimal interesesCausadosBase;
        private BigDecimal otrosBase;

        private BigDecimal baseCalculo;

        // =====================================================
        // Porcentajes
        // =====================================================

        private BigDecimal porcentajeBaseSeguro;
        private BigDecimal porcentajeExtraprima;
        private BigDecimal porcentajeAplicar;

        // =====================================================
        // Condiciones aplicadas
        // =====================================================

        private Boolean sobreSaldoActual;
        private Boolean incluyeInteresesCausados;

        // =====================================================
        // Movimiento
        // =====================================================

        private BigDecimal valorDebito;
        private BigDecimal valorCredito;
        private BigDecimal valorMovimiento;

        private String naturalezaMovimiento;

        // =====================================================
        // Saldo
        // =====================================================

        private BigDecimal saldoSeguroAcumuladoCredito;
        private BigDecimal saldoSeguroAcumuladoConfiguracion;

        // =====================================================
        // Comprobante
        // =====================================================

        private String tipoComprobante;
        private String numeroComprobante;
        private String comprobanteCompleto;

        // =====================================================
        // Estado
        // =====================================================

        private String estado;
        private Boolean movimientoActivo;

        // =====================================================
        // Secuencia
        // =====================================================

        private Long numeroMovimientoCredito;
        private Long numeroMovimientoSeguro;

        private Long cantidadMovimientosCredito;
        private Long cantidadMovimientosSeguro;

        // =====================================================
        // Observación
        // =====================================================

        private String observacionSeguro;

        // =====================================================
        // Auditoría
        // =====================================================

        private Integer fkSeguridadCreacion;
        private LocalDateTime fechaCreacion;

        private Integer fkSeguridadEdicion;
        private LocalDateTime fechaEdicion;
    }
}