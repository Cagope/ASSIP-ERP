package co.assip.erp.cartera.consultacreditos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Detalle funcional de un crédito seleccionado.
 *
 * Fuente:
 * cartera.vw_cartera_creditos_total
 *
 * Este DTO:
 *
 * - No calcula saldos.
 * - No recalcula riesgos.
 * - No reconstruye fechas.
 * - No combina conceptos financieros.
 * - Expone directamente la información entregada por la vista.
 */
@Getter
@Setter
public class ConsultaCreditoDetalleDTO {

    // =========================================================
    // Identificación del crédito
    // =========================================================

    private Integer idCarteraCredito;
    private String pagareCartera;
    private Integer idDatosPersonal;
    private Integer idCuentaAportes;

    private String tipoComprobante;
    private String numeroComprobante;

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
    // Destino económico
    // =========================================================

    private String codigoDestinoEconomico;
    private String descripcionDestinoEconomico;

    // =========================================================
    // Modalidad de interés
    // =========================================================

    private String periodoCodigoInteres;
    private String tipoModalidadInteres;
    private String descripcionModalidadInteres;
    private Integer periodoInteresMeses;

    // =========================================================
    // Condiciones de amortización
    // =========================================================

    private Integer amortizacionCapital;

    private String codigoTipoCuota;
    private String descripcionTipoCuota;

    private Integer plazo;
    private Integer mesesGraciaCapital;
    private Integer mesesGraciaInteres;

    private String codigoFormaPago;
    private String descripcionFormaPago;

    // =========================================================
    // Libranza y aprobación
    // =========================================================

    private Integer idEmpresaLibranza;

    private Integer idEnteAprobacion;
    private String nombreEnteAprobacion;

    // =========================================================
    // Estado de cartera
    // =========================================================

    private String codigoEstadoCartera;
    private String descripcionEstadoCartera;
    private String operativoEstadoCartera;

    // =========================================================
    // Estado jurídico
    // =========================================================

    private String codigoEstadoJuridico;
    private String descripcionEstadoJuridico;
    private LocalDate fechaEstadoJuridico;

    // =========================================================
    // Valores principales
    // =========================================================

    private BigDecimal valorInicialCredito;
    private BigDecimal valorDesembolsado;
    private BigDecimal valorBaseCalculoCuota;

    private BigDecimal valorPrimeraCuota;
    private BigDecimal valorCuota;

    // =========================================================
    // Saldos
    // =========================================================

    private BigDecimal saldoActual;
    private BigDecimal abonosPendientes;
    private BigDecimal saldoNetoPendiente;

    // =========================================================
    // Ejecución del crédito
    // =========================================================

    private BigDecimal porcentajeSaldoSobreDesembolso;
    private BigDecimal porcentajeCapitalAmortizado;
    private BigDecimal capitalAmortizadoEstimado;

    private Integer alturaCuota;
    private Integer cuotasPendientesEstimadas;

    private BigDecimal porcentajePlazoTranscurrido;

    // =========================================================
    // Tasas
    // =========================================================

    private BigDecimal tasaNominalAnual;
    private BigDecimal tasaEfectivaAnual;

    // =========================================================
    // Fechas principales
    // =========================================================

    private LocalDate fechaInclusionSistema;
    private LocalDate fechaContable;
    private LocalDate fechaDesembolso;

    private LocalDate fechaPrimeraCuota;
    private LocalDate fechaPrimeraCuotaCapital;
    private LocalDate fechaPrimeraCuotaInteres;

    private LocalDate fechaFinal;

    // =========================================================
    // Indicadores de fechas
    // =========================================================

    private Integer diasDesdeDesembolso;
    private Integer diasParaVencimiento;
    private Integer diasVencidoPorFechaFinal;

    private Boolean vencidoPorFechaFinal;

    // =========================================================
    // Últimas fechas por concepto
    // =========================================================

    private LocalDate ultimaFechaCapital;
    private LocalDate ultimaFechaInteres;
    private LocalDate ultimaFechaMora;
    private LocalDate ultimaFechaSeguro;
    private LocalDate ultimaFechaFondo;

    // =========================================================
    // Próximas fechas por concepto
    // =========================================================

    private LocalDate proximaFechaCapital;
    private LocalDate proximaFechaInteres;
    private LocalDate proximaFechaSeguro;
    private LocalDate proximaFechaFondo;

    private LocalDate interesesPagadosHasta;
    private LocalDate interesesMoraHasta;

    // =========================================================
    // Días para próximos vencimientos
    // =========================================================

    private Integer diasParaProximoCapital;
    private Integer diasParaProximoInteres;
    private Integer diasParaProximoSeguro;
    private Integer diasParaProximoFondo;

    // =========================================================
    // Última evaluación registrada en el crédito
    // =========================================================

    private Boolean creditoEvaluado;
    private LocalDate fechaEvaluacion;
    private Integer diasDesdeUltimaEvaluacion;
    private String comentarioEvaluacion;

    // =========================================================
    // Riesgo
    // =========================================================

    private String edadDeRiesgo;
    private String descripcionEdadDeRiesgo;

    private String edadRiesgoInicial;
    private String descripcionEdadRiesgoInicial;

    private String edadDeMora;
    private String descripcionEdadDeMora;

    private String edadDePe;
    private String descripcionEdadDePe;

    private String edadDeHomologacion;
    private String descripcionEdadDeHomologacion;

    private String edadContable;
    private String descripcionEdadContable;

    private Integer ordenEdadRiesgo;
    private Integer ordenEdadMora;
    private Integer ordenEdadContable;

    // =========================================================
    // Indicadores de riesgo
    // =========================================================

    private Boolean creditoEnMora;
    private Boolean riesgoAlto;
    private Boolean moraAlta;
    private Boolean deterioroContableAlto;

    private Boolean riesgoDeterioradoDesdeInicial;
    private Boolean riesgoMejoradoDesdeInicial;

    // =========================================================
    // Modificaciones del crédito
    // =========================================================

    private String codigoModificacionCredito;
    private String descripcionModificacionCredito;

    private Integer numeroNovaciones;

    private Boolean creditoNovado;
    private Boolean creditoReestructurado;

    private LocalDate fechaReestructuracion;

    private String edadReestructurado;
    private String descripcionEdadReestructurado;

    private String comentarioRestructuracion;
    private Integer diasDesdeReestructuracion;

    // =========================================================
    // Consultas a centrales de riesgo
    // =========================================================

    private LocalDate ultimaFechaCifin;
    private LocalDate ultimaFechaDatacredito;
    private LocalDate ultimaFechaOtra;

    private Integer diasDesdeConsultaCifin;
    private Integer diasDesdeConsultaDatacredito;
    private Integer diasDesdeOtraConsulta;

    // =========================================================
    // Información complementaria
    // =========================================================

    private String establecimiento;
    private String comentarioGeneral;

    // =========================================================
    // Estado financiero y operativo
    // =========================================================

    private Boolean tieneSaldo;
    private Boolean creditoSaldado;
    private Boolean tieneAbonosPendientes;
    private Boolean enCobroJuridico;

    // =========================================================
    // Alertas de la vista
    // =========================================================

    private Boolean requiereRevision;

    private String nivelAlertaCredito;
    private Integer ordenAlertaCredito;
    private String motivoAlertaCredito;

    // =========================================================
    // Auditoría
    // =========================================================

    private Integer fkSeguridadCreacion;
    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;
    private LocalDateTime fechaEdicion;
}