package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
public class ExpedienteCreditoDTO {

    // =========================================================
    // Identificación del asociado
    // =========================================================
    private Long idDatosPersonal;

    private String tipoDocumento;
    private String nombreTipoDocumento;
    private String documento;

    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String nombreCompleto;

    // =========================================================
    // Identificación del crédito
    // =========================================================
    private Long idCredito;

    private String numeroCredito;
    private String numeroSolicitud;

    private String referenciaCredito;
    private String descripcionCredito;

    // =========================================================
    // Fábrica u origen del crédito
    // =========================================================
    private Long idFabricaCredito;

    private String codigoFabricaCredito;
    private String nombreFabricaCredito;

    private String origenCredito;

    // =========================================================
    // Línea y producto
    // =========================================================
    private Long idLineaCredito;

    private String codigoLineaCredito;
    private String nombreLineaCredito;

    private Long idTipoProducto;
    private String codigoTipoProducto;
    private String nombreTipoProducto;

    private Long idClasificacionCredito;
    private String codigoClasificacionCredito;
    private String nombreClasificacionCredito;

    // =========================================================
    // Estado del crédito
    // =========================================================
    private Long idEstadoCartera;

    private String codigoEstadoCartera;
    private String nombreEstadoCartera;

    private Boolean vigente;
    private Boolean cancelado;
    private Boolean castigado;
    private Boolean enMora;
    private Boolean reestructurado;
    private Boolean novado;
    private Boolean alivio;
    private Boolean refinanciado;

    // =========================================================
    // Fechas principales
    // =========================================================
    private LocalDate fechaSolicitud;
    private LocalDate fechaAprobacion;
    private LocalDate fechaDesembolso;

    private LocalDate fechaPrimerVencimiento;
    private LocalDate fechaVencimiento;
    private LocalDate fechaProximoPago;

    private LocalDate fechaUltimoPago;
    private LocalDate fechaCancelacion;

    private Integer diasDesdeDesembolso;
    private Integer diasParaProximoPago;

    // =========================================================
    // Condiciones iniciales
    // =========================================================
    private BigDecimal valorSolicitado;
    private BigDecimal valorAprobado;
    private BigDecimal valorDesembolsado;

    private Integer plazoInicial;
    private Integer plazo;

    private Integer numeroCuotasInicial;
    private Integer numeroCuotasPagadas;
    private Integer numeroCuotasPendientes;
    private Integer numeroCuotasVencidas;

    // =========================================================
    // Tipo de cuota y forma de pago
    // =========================================================
    private Long idTipoCuota;
    private String codigoTipoCuota;
    private String nombreTipoCuota;

    private Long idFormaPago;
    private String codigoFormaPago;
    private String nombreFormaPago;

    private String periodicidadPago;

    private BigDecimal valorCuotaInicial;
    private BigDecimal valorCuota;

    // =========================================================
    // Modalidad y tasas
    // =========================================================
    private Long idModalidadInteres;
    private String codigoModalidadInteres;
    private String nombreModalidadInteres;

    private BigDecimal tasaNominal;
    private BigDecimal tasaEfectivaAnual;
    private BigDecimal tasaMora;

    private BigDecimal tasaRedescuento;
    private BigDecimal margenRedescuento;

    // =========================================================
    // Saldos
    // =========================================================
    private BigDecimal saldoActual;

    private BigDecimal saldoInteresCorriente;
    private BigDecimal saldoInteresMora;

    private BigDecimal saldoSeguro;
    private BigDecimal saldoOtrosConceptos;

    private BigDecimal saldoTotal;

    private BigDecimal capitalVencido;
    private BigDecimal interesesVencidos;

    private BigDecimal valorVencidoTotal;

    // =========================================================
    // Pagos
    // =========================================================
    private BigDecimal totalPagadoCapital;
    private BigDecimal totalPagadoIntereses;
    private BigDecimal totalPagadoMora;
    private BigDecimal totalPagadoSeguros;
    private BigDecimal totalPagadoOtros;

    private BigDecimal totalPagado;

    private BigDecimal ultimoValorPagado;

    // =========================================================
    // Mora
    // =========================================================
    private Integer diasMora;
    private Integer edadMora;

    private String rangoMora;

    private Boolean moraLeve;
    private Boolean moraModerada;
    private Boolean moraGrave;

    // =========================================================
    // Edad y clasificación de riesgo
    // =========================================================
    private String edadRiesgoInicial;
    private String edadRiesgo;

    private String edadRiesgoEvaluada;
    private String edadRiesgoFinal;

    private String codigoCalificacion;
    private String nombreCalificacion;

    private String resultadoEvaluacion;

    /*
     * Valores manejados en el proceso de evaluación:
     *
     * R = Recalificar
     * H = Habilitar
     * M = Mantener
     */
    private String accionEvaluacion;

    private LocalDate fechaUltimaEvaluacion;
    private LocalDate fechaProximaEvaluacion;

    private Boolean requiereEvaluacion;
    private Boolean evaluacionVencida;

    // =========================================================
    // Provisiones y deterioro
    // =========================================================
    private BigDecimal porcentajeProvisionCapital;
    private BigDecimal porcentajeProvisionIntereses;

    private BigDecimal provisionCapital;
    private BigDecimal provisionIntereses;
    private BigDecimal provisionGeneral;

    private BigDecimal provisionTotal;

    private BigDecimal saldoNetoDespuesProvision;

    // =========================================================
    // Garantía principal
    // =========================================================
    private Long idTipoGarantiaCredito;

    private String codigoGarantia;
    private String nombreGarantia;

    private Boolean garantiaIdonea;
    private Boolean tieneGarantiaReal;

    private Integer cantidadGarantias;

    private BigDecimal valorGarantias;
    private BigDecimal valorGarantiasAdmisible;

    private BigDecimal porcentajeCoberturaGarantias;

    private BigDecimal excesoGarantia;
    private BigDecimal faltanteGarantia;

    private Boolean garantiaSuficiente;
    private Boolean garantiaInsuficiente;

    // =========================================================
    // Condiciones de garantía y respaldo
    // =========================================================
    private Boolean respaldadoConAportes;
    private BigDecimal valorAportesRespaldo;

    private Boolean respaldadoConAhorros;
    private BigDecimal valorAhorrosRespaldo;

    private Boolean respaldadoConCdat;
    private BigDecimal valorCdatRespaldo;

    private BigDecimal totalRespaldosLiquidos;

    // =========================================================
    // Redescuento
    // =========================================================
    private Long idEntidadRedescuento;

    private String codigoEntidadRedescuento;
    private String nombreEntidadRedescuento;

    private Boolean creditoRedescontado;

    private BigDecimal valorRedescuento;
    private BigDecimal saldoRedescuento;

    // =========================================================
    // Vivienda
    // =========================================================
    private Long idClaseViviendaSes;
    private String codigoClaseViviendaSes;
    private String nombreClaseViviendaSes;

    private Boolean viviendaVis;

    private Long idTipoVis;
    private String codigoTipoVis;
    private String nombreTipoVis;

    private Boolean tieneSubsidio;

    private Long idSenalSubsidio;
    private String codigoSenalSubsidio;
    private String nombreSenalSubsidio;

    // =========================================================
    // Desembolso
    // =========================================================
    private Long idSujetoDesembolso;

    private String codigoSujetoDesembolso;
    private String nombreSujetoDesembolso;

    private String cuentaDesembolso;
    private String entidadDesembolso;

    // =========================================================
    // Modificaciones del crédito
    // =========================================================
    private Long idModificacionCredito;

    private String codigoModificacionCredito;
    private String nombreModificacionCredito;

    private LocalDate fechaUltimaModificacion;

    private Integer cantidadModificaciones;

    private String observacionesModificacion;

    // =========================================================
    // Cobranza y estado jurídico
    // =========================================================
    private Long idEstadoJuridico;

    private String codigoEstadoJuridico;
    private String nombreEstadoJuridico;

    private Boolean enCobranza;
    private Boolean prejuridico;
    private Boolean juridico;
    private Boolean insolvente;

    private LocalDate fechaInicioCobranza;
    private LocalDate fechaInicioJuridico;

    private String abogadoResponsable;
    private String numeroProcesoJuridico;

    // =========================================================
    // Reciprocidad
    // =========================================================
    private BigDecimal porcentajeReciprocidadExigido;
    private BigDecimal valorReciprocidadExigido;

    private BigDecimal valorReciprocidadActual;

    private BigDecimal porcentajeCumplimientoReciprocidad;

    private Boolean reciprocidadCumplida;
    private BigDecimal faltanteReciprocidad;

    // =========================================================
    // Capacidad de pago
    // =========================================================
    private BigDecimal ingresosMensuales;
    private BigDecimal egresosMensuales;

    private BigDecimal capacidadPagoDisponible;
    private BigDecimal cuotaTotalMensual;

    private BigDecimal porcentajeCompromisoIngresos;

    private Boolean capacidadPagoSuficiente;
    private Boolean sobreendeudamiento;

    // =========================================================
    // Seguros asociados al crédito
    // =========================================================
    private Boolean tieneSeguroVida;
    private Boolean tieneSeguroDeudores;
    private Boolean tieneSeguroGarantia;

    private BigDecimal valorSeguroMensual;

    private LocalDate fechaVencimientoSeguro;

    private Boolean seguroVigente;
    private Boolean seguroProximoVencer;
    private Boolean seguroVencido;

    private Integer diasParaVencimientoSeguro;

    // =========================================================
    // Extracto y amortización
    // =========================================================
    private Boolean permiteGenerarExtracto;
    private Boolean permiteGenerarTablaAmortizacion;

    private Boolean tienePlanPagos;
    private Integer cantidadRegistrosPlanPagos;

    private LocalDate fechaUltimoExtracto;

    // =========================================================
    // Indicadores gerenciales
    // =========================================================
    private BigDecimal porcentajePagadoCapital;
    private BigDecimal porcentajeSaldoCapital;

    private BigDecimal porcentajePlazoTranscurrido;
    private BigDecimal porcentajeCuotasPagadas;

    private BigDecimal exposicionNeta;

    private String nivelRiesgo;
    private String colorRiesgo;

    // =========================================================
    // Calidad de información
    // =========================================================
    private Boolean informacionGeneralCompleta;
    private Boolean informacionFinancieraCompleta;
    private Boolean informacionPagosCompleta;
    private Boolean informacionGarantiasCompleta;
    private Boolean informacionRiesgoCompleta;

    private Boolean informacionCompleta;
    private Integer porcentajeCompletitud;

    // =========================================================
    // Alertas gerenciales
    // =========================================================
    private Boolean requiereRevision;
    private Boolean requiereGestionCobranza;
    private Boolean requiereActualizacionGarantias;

    private Integer cantidadAlertas;

    private Integer alertasCriticas;
    private Integer alertasAdvertencia;
    private Integer alertasInformativas;

    private String nivelAlerta;
    private String resumenAlertas;
    private String motivoRevision;

    // =========================================================
    // Agencia y responsable
    // =========================================================
    private Long idAgencia;

    private String codigoAgencia;
    private String nombreAgencia;

    private Integer idAsesor;
    private String nombreAsesor;

    // =========================================================
    // Observaciones
    // =========================================================
    private String observaciones;

    // =========================================================
    // Auditoría
    // =========================================================
    private Integer fkSeguridadCreacion;
    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;
    private LocalDateTime fechaEdicion;

    // =========================================================
    // Constructor
    // =========================================================
    public ExpedienteCreditoDTO() {

        this.vigente = Boolean.FALSE;
        this.cancelado = Boolean.FALSE;
        this.castigado = Boolean.FALSE;
        this.enMora = Boolean.FALSE;

        this.reestructurado = Boolean.FALSE;
        this.novado = Boolean.FALSE;
        this.alivio = Boolean.FALSE;
        this.refinanciado = Boolean.FALSE;

        this.diasDesdeDesembolso = 0;
        this.diasParaProximoPago = 0;

        this.valorSolicitado = BigDecimal.ZERO;
        this.valorAprobado = BigDecimal.ZERO;
        this.valorDesembolsado = BigDecimal.ZERO;

        this.plazoInicial = 0;
        this.plazo = 0;

        this.numeroCuotasInicial = 0;
        this.numeroCuotasPagadas = 0;
        this.numeroCuotasPendientes = 0;
        this.numeroCuotasVencidas = 0;

        this.valorCuotaInicial = BigDecimal.ZERO;
        this.valorCuota = BigDecimal.ZERO;

        this.tasaNominal = BigDecimal.ZERO;
        this.tasaEfectivaAnual = BigDecimal.ZERO;
        this.tasaMora = BigDecimal.ZERO;

        this.tasaRedescuento = BigDecimal.ZERO;
        this.margenRedescuento = BigDecimal.ZERO;

        this.saldoActual = BigDecimal.ZERO;
        this.saldoInteresCorriente = BigDecimal.ZERO;
        this.saldoInteresMora = BigDecimal.ZERO;
        this.saldoSeguro = BigDecimal.ZERO;
        this.saldoOtrosConceptos = BigDecimal.ZERO;
        this.saldoTotal = BigDecimal.ZERO;

        this.capitalVencido = BigDecimal.ZERO;
        this.interesesVencidos = BigDecimal.ZERO;
        this.valorVencidoTotal = BigDecimal.ZERO;

        this.totalPagadoCapital = BigDecimal.ZERO;
        this.totalPagadoIntereses = BigDecimal.ZERO;
        this.totalPagadoMora = BigDecimal.ZERO;
        this.totalPagadoSeguros = BigDecimal.ZERO;
        this.totalPagadoOtros = BigDecimal.ZERO;
        this.totalPagado = BigDecimal.ZERO;
        this.ultimoValorPagado = BigDecimal.ZERO;

        this.diasMora = 0;
        this.edadMora = 0;

        this.moraLeve = Boolean.FALSE;
        this.moraModerada = Boolean.FALSE;
        this.moraGrave = Boolean.FALSE;

        this.requiereEvaluacion = Boolean.FALSE;
        this.evaluacionVencida = Boolean.FALSE;

        this.porcentajeProvisionCapital = BigDecimal.ZERO;
        this.porcentajeProvisionIntereses = BigDecimal.ZERO;

        this.provisionCapital = BigDecimal.ZERO;
        this.provisionIntereses = BigDecimal.ZERO;
        this.provisionGeneral = BigDecimal.ZERO;
        this.provisionTotal = BigDecimal.ZERO;

        this.saldoNetoDespuesProvision = BigDecimal.ZERO;

        this.garantiaIdonea = Boolean.FALSE;
        this.tieneGarantiaReal = Boolean.FALSE;

        this.cantidadGarantias = 0;

        this.valorGarantias = BigDecimal.ZERO;
        this.valorGarantiasAdmisible = BigDecimal.ZERO;
        this.porcentajeCoberturaGarantias = BigDecimal.ZERO;

        this.excesoGarantia = BigDecimal.ZERO;
        this.faltanteGarantia = BigDecimal.ZERO;

        this.garantiaSuficiente = Boolean.FALSE;
        this.garantiaInsuficiente = Boolean.FALSE;

        this.respaldadoConAportes = Boolean.FALSE;
        this.valorAportesRespaldo = BigDecimal.ZERO;

        this.respaldadoConAhorros = Boolean.FALSE;
        this.valorAhorrosRespaldo = BigDecimal.ZERO;

        this.respaldadoConCdat = Boolean.FALSE;
        this.valorCdatRespaldo = BigDecimal.ZERO;

        this.totalRespaldosLiquidos = BigDecimal.ZERO;

        this.creditoRedescontado = Boolean.FALSE;
        this.valorRedescuento = BigDecimal.ZERO;
        this.saldoRedescuento = BigDecimal.ZERO;

        this.viviendaVis = Boolean.FALSE;
        this.tieneSubsidio = Boolean.FALSE;

        this.cantidadModificaciones = 0;

        this.enCobranza = Boolean.FALSE;
        this.prejuridico = Boolean.FALSE;
        this.juridico = Boolean.FALSE;
        this.insolvente = Boolean.FALSE;

        this.porcentajeReciprocidadExigido = BigDecimal.ZERO;
        this.valorReciprocidadExigido = BigDecimal.ZERO;
        this.valorReciprocidadActual = BigDecimal.ZERO;

        this.porcentajeCumplimientoReciprocidad = BigDecimal.ZERO;

        this.reciprocidadCumplida = Boolean.FALSE;
        this.faltanteReciprocidad = BigDecimal.ZERO;

        this.ingresosMensuales = BigDecimal.ZERO;
        this.egresosMensuales = BigDecimal.ZERO;

        this.capacidadPagoDisponible = BigDecimal.ZERO;
        this.cuotaTotalMensual = BigDecimal.ZERO;

        this.porcentajeCompromisoIngresos = BigDecimal.ZERO;

        this.capacidadPagoSuficiente = Boolean.FALSE;
        this.sobreendeudamiento = Boolean.FALSE;

        this.tieneSeguroVida = Boolean.FALSE;
        this.tieneSeguroDeudores = Boolean.FALSE;
        this.tieneSeguroGarantia = Boolean.FALSE;

        this.valorSeguroMensual = BigDecimal.ZERO;

        this.seguroVigente = Boolean.FALSE;
        this.seguroProximoVencer = Boolean.FALSE;
        this.seguroVencido = Boolean.FALSE;

        this.diasParaVencimientoSeguro = 0;

        this.permiteGenerarExtracto = Boolean.TRUE;
        this.permiteGenerarTablaAmortizacion = Boolean.TRUE;

        this.tienePlanPagos = Boolean.FALSE;
        this.cantidadRegistrosPlanPagos = 0;

        this.porcentajePagadoCapital = BigDecimal.ZERO;
        this.porcentajeSaldoCapital = BigDecimal.ZERO;

        this.porcentajePlazoTranscurrido = BigDecimal.ZERO;
        this.porcentajeCuotasPagadas = BigDecimal.ZERO;

        this.exposicionNeta = BigDecimal.ZERO;

        this.informacionGeneralCompleta = Boolean.FALSE;
        this.informacionFinancieraCompleta = Boolean.FALSE;
        this.informacionPagosCompleta = Boolean.FALSE;
        this.informacionGarantiasCompleta = Boolean.FALSE;
        this.informacionRiesgoCompleta = Boolean.FALSE;

        this.informacionCompleta = Boolean.FALSE;
        this.porcentajeCompletitud = 0;

        this.requiereRevision = Boolean.FALSE;
        this.requiereGestionCobranza = Boolean.FALSE;
        this.requiereActualizacionGarantias = Boolean.FALSE;

        this.cantidadAlertas = 0;
        this.alertasCriticas = 0;
        this.alertasAdvertencia = 0;
        this.alertasInformativas = 0;
    }

    // =========================================================
    // Cálculos de saldo
    // =========================================================

    /**
     * Calcula el saldo total vigente del crédito.
     */
    public BigDecimal calcularSaldoTotal() {

        this.saldoTotal =
                valorSeguro(saldoActual)
                        .add(valorSeguro(saldoInteresCorriente))
                        .add(valorSeguro(saldoInteresMora))
                        .add(valorSeguro(saldoSeguro))
                        .add(valorSeguro(saldoOtrosConceptos));

        return this.saldoTotal;
    }

    /**
     * Calcula el valor total actualmente vencido.
     */
    public BigDecimal calcularValorVencidoTotal() {

        this.valorVencidoTotal =
                valorSeguro(capitalVencido)
                        .add(valorSeguro(interesesVencidos))
                        .add(valorSeguro(saldoInteresMora));

        return this.valorVencidoTotal;
    }

    /**
     * Consolida todos los pagos registrados.
     */
    public BigDecimal calcularTotalPagado() {

        this.totalPagado =
                valorSeguro(totalPagadoCapital)
                        .add(valorSeguro(totalPagadoIntereses))
                        .add(valorSeguro(totalPagadoMora))
                        .add(valorSeguro(totalPagadoSeguros))
                        .add(valorSeguro(totalPagadoOtros));

        return this.totalPagado;
    }

    // =========================================================
    // Fechas y plazo
    // =========================================================

    /**
     * Calcula indicadores temporales básicos del crédito.
     */
    public void calcularIndicadoresFecha() {

        LocalDate hoy =
                LocalDate.now();

        if (fechaDesembolso != null) {

            long dias =
                    ChronoUnit.DAYS.between(
                            fechaDesembolso,
                            hoy
                    );

            this.diasDesdeDesembolso =
                    convertirEnteroSeguro(dias);
        }

        if (fechaProximoPago != null) {

            long dias =
                    ChronoUnit.DAYS.between(
                            hoy,
                            fechaProximoPago
                    );

            this.diasParaProximoPago =
                    convertirEnteroSeguro(dias);
        }

        if (fechaProximaEvaluacion != null) {

            this.evaluacionVencida =
                    fechaProximaEvaluacion.isBefore(hoy);
        }
    }

    // =========================================================
    // Mora
    // =========================================================

    /**
     * Clasifica la mora para presentación gerencial.
     *
     * Los rangos pueden ajustarse posteriormente con parámetros
     * institucionales.
     */
    public void evaluarMora() {

        int dias =
                diasMora == null
                        ? 0
                        : Math.max(diasMora, 0);

        this.enMora =
                dias > 0
                        || valorSeguro(valorVencidoTotal)
                        .compareTo(BigDecimal.ZERO) > 0;

        this.moraLeve = Boolean.FALSE;
        this.moraModerada = Boolean.FALSE;
        this.moraGrave = Boolean.FALSE;

        if (dias == 0) {

            this.rangoMora = "AL_DIA";

        } else if (dias <= 30) {

            this.moraLeve = Boolean.TRUE;
            this.rangoMora = "1_A_30";

        } else if (dias <= 90) {

            this.moraModerada = Boolean.TRUE;
            this.rangoMora = "31_A_90";

        } else {

            this.moraGrave = Boolean.TRUE;
            this.rangoMora = "MAYOR_A_90";
        }

        this.requiereGestionCobranza =
                Boolean.TRUE.equals(enMora);
    }

    // =========================================================
    // Provisiones
    // =========================================================

    /**
     * Consolida la provisión total y calcula el saldo neto.
     */
    public BigDecimal calcularProvisionTotal() {

        this.provisionTotal =
                valorSeguro(provisionCapital)
                        .add(valorSeguro(provisionIntereses))
                        .add(valorSeguro(provisionGeneral));

        this.saldoNetoDespuesProvision =
                valorSeguro(saldoTotal)
                        .subtract(this.provisionTotal);

        return this.provisionTotal;
    }

    // =========================================================
    // Garantías
    // =========================================================

    /**
     * Calcula el porcentaje de cobertura total de las garantías.
     */
    public BigDecimal calcularCoberturaGarantias() {

        BigDecimal saldo =
                valorSeguro(saldoTotal);

        BigDecimal cobertura =
                valorSeguro(valorGarantiasAdmisible);

        this.excesoGarantia =
                cobertura.subtract(saldo)
                        .max(BigDecimal.ZERO);

        this.faltanteGarantia =
                saldo.subtract(cobertura)
                        .max(BigDecimal.ZERO);

        this.garantiaSuficiente = Boolean.FALSE;
        this.garantiaInsuficiente = Boolean.FALSE;

        if (saldo.compareTo(BigDecimal.ZERO) <= 0) {

            this.porcentajeCoberturaGarantias =
                    BigDecimal.ZERO;

            this.garantiaSuficiente =
                    Boolean.TRUE;

            return this.porcentajeCoberturaGarantias;
        }

        this.porcentajeCoberturaGarantias =
                cobertura
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                saldo,
                                2,
                                RoundingMode.HALF_UP
                        );

        this.garantiaSuficiente =
                cobertura.compareTo(saldo) >= 0;

        this.garantiaInsuficiente =
                !Boolean.TRUE.equals(garantiaSuficiente);

        return this.porcentajeCoberturaGarantias;
    }

    /**
     * Consolida los respaldos líquidos del asociado.
     */
    public BigDecimal calcularRespaldosLiquidos() {

        this.totalRespaldosLiquidos =
                valorSeguro(valorAportesRespaldo)
                        .add(valorSeguro(valorAhorrosRespaldo))
                        .add(valorSeguro(valorCdatRespaldo));

        this.respaldadoConAportes =
                valorSeguro(valorAportesRespaldo)
                        .compareTo(BigDecimal.ZERO) > 0;

        this.respaldadoConAhorros =
                valorSeguro(valorAhorrosRespaldo)
                        .compareTo(BigDecimal.ZERO) > 0;

        this.respaldadoConCdat =
                valorSeguro(valorCdatRespaldo)
                        .compareTo(BigDecimal.ZERO) > 0;

        return this.totalRespaldosLiquidos;
    }

    // =========================================================
    // Reciprocidad
    // =========================================================

    /**
     * Evalúa el cumplimiento de reciprocidad.
     */
    public BigDecimal evaluarReciprocidad() {

        BigDecimal exigido =
                valorSeguro(valorReciprocidadExigido);

        BigDecimal actual =
                valorSeguro(valorReciprocidadActual);

        this.faltanteReciprocidad =
                exigido.subtract(actual)
                        .max(BigDecimal.ZERO);

        if (exigido.compareTo(BigDecimal.ZERO) <= 0) {

            this.porcentajeCumplimientoReciprocidad =
                    BigDecimal.valueOf(100);

            this.reciprocidadCumplida =
                    Boolean.TRUE;

            return this.porcentajeCumplimientoReciprocidad;
        }

        this.porcentajeCumplimientoReciprocidad =
                actual
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                exigido,
                                2,
                                RoundingMode.HALF_UP
                        );

        this.reciprocidadCumplida =
                actual.compareTo(exigido) >= 0;

        return this.porcentajeCumplimientoReciprocidad;
    }

    // =========================================================
    // Capacidad de pago
    // =========================================================

    /**
     * Calcula la capacidad de pago disponible y el compromiso
     * mensual de los ingresos.
     */
    public BigDecimal evaluarCapacidadPago() {

        this.capacidadPagoDisponible =
                valorSeguro(ingresosMensuales)
                        .subtract(valorSeguro(egresosMensuales))
                        .subtract(valorSeguro(cuotaTotalMensual));

        BigDecimal ingresos =
                valorSeguro(ingresosMensuales);

        if (ingresos.compareTo(BigDecimal.ZERO) <= 0) {

            this.porcentajeCompromisoIngresos =
                    BigDecimal.ZERO;

            this.capacidadPagoSuficiente =
                    Boolean.FALSE;

            this.sobreendeudamiento =
                    valorSeguro(cuotaTotalMensual)
                            .compareTo(BigDecimal.ZERO) > 0;

            return this.capacidadPagoDisponible;
        }

        this.porcentajeCompromisoIngresos =
                valorSeguro(cuotaTotalMensual)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                ingresos,
                                2,
                                RoundingMode.HALF_UP
                        );

        this.capacidadPagoSuficiente =
                this.capacidadPagoDisponible
                        .compareTo(BigDecimal.ZERO) >= 0;

        this.sobreendeudamiento =
                !Boolean.TRUE.equals(
                        capacidadPagoSuficiente
                );

        return this.capacidadPagoDisponible;
    }

    // =========================================================
    // Seguro
    // =========================================================

    /**
     * Evalúa la vigencia del seguro relacionado con el crédito.
     */
    public void evaluarSeguro(
            int diasAdvertencia
    ) {

        this.seguroVigente = Boolean.FALSE;
        this.seguroProximoVencer = Boolean.FALSE;
        this.seguroVencido = Boolean.FALSE;
        this.diasParaVencimientoSeguro = 0;

        if (fechaVencimientoSeguro == null) {
            return;
        }

        long dias =
                ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        fechaVencimientoSeguro
                );

        this.diasParaVencimientoSeguro =
                convertirEnteroSeguro(dias);

        int advertencia =
                Math.max(diasAdvertencia, 0);

        if (dias < 0) {

            this.seguroVencido = Boolean.TRUE;

        } else if (dias <= advertencia) {

            this.seguroVigente = Boolean.TRUE;
            this.seguroProximoVencer = Boolean.TRUE;

        } else {

            this.seguroVigente = Boolean.TRUE;
        }
    }

    // =========================================================
    // Indicadores de ejecución
    // =========================================================

    /**
     * Calcula los principales porcentajes de ejecución.
     */
    public void calcularIndicadoresEjecucion() {

        BigDecimal desembolsado =
                valorSeguro(valorDesembolsado);

        BigDecimal capitalPagado =
                valorSeguro(totalPagadoCapital);

        BigDecimal capitalPendiente =
                valorSeguro(saldoActual);

        if (desembolsado.compareTo(BigDecimal.ZERO) > 0) {

            this.porcentajePagadoCapital =
                    capitalPagado
                            .multiply(BigDecimal.valueOf(100))
                            .divide(
                                    desembolsado,
                                    2,
                                    RoundingMode.HALF_UP
                            );

            this.porcentajeSaldoCapital =
                    capitalPendiente
                            .multiply(BigDecimal.valueOf(100))
                            .divide(
                                    desembolsado,
                                    2,
                                    RoundingMode.HALF_UP
                            );

        } else {

            this.porcentajePagadoCapital =
                    BigDecimal.ZERO;

            this.porcentajeSaldoCapital =
                    BigDecimal.ZERO;
        }

        if (numeroCuotasInicial != null
                && numeroCuotasInicial > 0) {

            this.porcentajeCuotasPagadas =
                    BigDecimal.valueOf(
                                    numeroCuotasPagadas == null
                                            ? 0
                                            : numeroCuotasPagadas
                            )
                            .multiply(BigDecimal.valueOf(100))
                            .divide(
                                    BigDecimal.valueOf(
                                            numeroCuotasInicial
                                    ),
                                    2,
                                    RoundingMode.HALF_UP
                            );

        } else {

            this.porcentajeCuotasPagadas =
                    BigDecimal.ZERO;
        }

        if (fechaDesembolso != null
                && fechaVencimiento != null
                && fechaVencimiento.isAfter(
                fechaDesembolso)) {

            long plazoTotal =
                    ChronoUnit.DAYS.between(
                            fechaDesembolso,
                            fechaVencimiento
                    );

            long plazoTranscurrido =
                    ChronoUnit.DAYS.between(
                            fechaDesembolso,
                            LocalDate.now()
                    );

            plazoTranscurrido =
                    Math.max(
                            0,
                            Math.min(
                                    plazoTranscurrido,
                                    plazoTotal
                            )
                    );

            this.porcentajePlazoTranscurrido =
                    BigDecimal.valueOf(
                                    plazoTranscurrido
                            )
                            .multiply(BigDecimal.valueOf(100))
                            .divide(
                                    BigDecimal.valueOf(
                                            plazoTotal
                                    ),
                                    2,
                                    RoundingMode.HALF_UP
                            );

        } else {

            this.porcentajePlazoTranscurrido =
                    BigDecimal.ZERO;
        }
    }

    /**
     * Calcula la exposición neta después de respaldos y garantías.
     */
    public BigDecimal calcularExposicionNeta() {

        BigDecimal coberturas =
                valorSeguro(valorGarantiasAdmisible)
                        .add(valorSeguro(totalRespaldosLiquidos));

        this.exposicionNeta =
                valorSeguro(saldoTotal)
                        .subtract(coberturas)
                        .max(BigDecimal.ZERO);

        return this.exposicionNeta;
    }

    // =========================================================
    // Calidad de información
    // =========================================================

    /**
     * Evalúa la completitud de los datos principales.
     */
    public int evaluarCompletitud() {

        this.informacionGeneralCompleta =
                idCredito != null
                        && tieneTexto(numeroCredito)
                        && idLineaCredito != null
                        && tieneTexto(codigoEstadoCartera);

        this.informacionFinancieraCompleta =
                valorDesembolsado != null
                        && saldoActual != null
                        && saldoTotal != null
                        && valorCuota != null;

        this.informacionPagosCompleta =
                numeroCuotasInicial != null
                        && numeroCuotasPagadas != null
                        && numeroCuotasPendientes != null;

        this.informacionGarantiasCompleta =
                cantidadGarantias != null
                        && (
                        cantidadGarantias == 0
                                || valorGarantiasAdmisible != null
                );

        this.informacionRiesgoCompleta =
                tieneTexto(edadRiesgo)
                        && diasMora != null;

        int totalValidaciones = 5;
        int validacionesCompletas = 0;

        if (Boolean.TRUE.equals(
                informacionGeneralCompleta)) {
            validacionesCompletas++;
        }

        if (Boolean.TRUE.equals(
                informacionFinancieraCompleta)) {
            validacionesCompletas++;
        }

        if (Boolean.TRUE.equals(
                informacionPagosCompleta)) {
            validacionesCompletas++;
        }

        if (Boolean.TRUE.equals(
                informacionGarantiasCompleta)) {
            validacionesCompletas++;
        }

        if (Boolean.TRUE.equals(
                informacionRiesgoCompleta)) {
            validacionesCompletas++;
        }

        this.porcentajeCompletitud =
                (validacionesCompletas * 100)
                        / totalValidaciones;

        this.informacionCompleta =
                validacionesCompletas
                        == totalValidaciones;

        return this.porcentajeCompletitud;
    }

    // =========================================================
    // Riesgo y alertas
    // =========================================================

    /**
     * Consolida los indicadores y alertas gerenciales.
     */
    public void evaluarEstadoGeneral(
            int diasAdvertenciaSeguro
    ) {

        calcularSaldoTotal();
        calcularValorVencidoTotal();
        calcularTotalPagado();

        calcularIndicadoresFecha();
        evaluarMora();
        calcularProvisionTotal();

        calcularCoberturaGarantias();
        calcularRespaldosLiquidos();

        evaluarReciprocidad();
        evaluarCapacidadPago();
        evaluarSeguro(diasAdvertenciaSeguro);

        calcularIndicadoresEjecucion();
        calcularExposicionNeta();
        evaluarCompletitud();

        int criticas = 0;
        int advertencias = 0;
        int informativas = 0;

        StringBuilder motivos =
                new StringBuilder();

        // -----------------------------------------------------
        // Alertas críticas
        // -----------------------------------------------------
        if (Boolean.TRUE.equals(moraGrave)) {

            criticas++;
            agregarMotivo(
                    motivos,
                    "Mora superior a 90 días"
            );
        }

        if (Boolean.TRUE.equals(juridico)) {

            criticas++;
            agregarMotivo(
                    motivos,
                    "Crédito en cobro jurídico"
            );
        }

        if (Boolean.TRUE.equals(insolvente)) {

            criticas++;
            agregarMotivo(
                    motivos,
                    "Asociado en estado de insolvencia"
            );
        }

        if (Boolean.TRUE.equals(castigado)) {

            criticas++;
            agregarMotivo(
                    motivos,
                    "Crédito castigado"
            );
        }

        if (Boolean.TRUE.equals(
                garantiaInsuficiente)
                && Boolean.TRUE.equals(
                tieneGarantiaReal)) {

            criticas++;
            agregarMotivo(
                    motivos,
                    "Garantías insuficientes"
            );
        }

        if (Boolean.TRUE.equals(
                seguroVencido)
                && (
                Boolean.TRUE.equals(tieneSeguroDeudores)
                        || Boolean.TRUE.equals(tieneSeguroGarantia)
        )) {

            criticas++;
            agregarMotivo(
                    motivos,
                    "Seguro del crédito vencido"
            );
        }

        // -----------------------------------------------------
        // Advertencias
        // -----------------------------------------------------
        if (Boolean.TRUE.equals(moraModerada)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Mora entre 31 y 90 días"
            );
        }

        if (Boolean.TRUE.equals(moraLeve)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Mora entre 1 y 30 días"
            );
        }

        if (Boolean.TRUE.equals(prejuridico)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Crédito en cobro prejurídico"
            );
        }

        if (Boolean.TRUE.equals(
                evaluacionVencida)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Evaluación de cartera vencida"
            );
        }

        if (Boolean.TRUE.equals(
                requiereEvaluacion)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Crédito pendiente de evaluación"
            );
        }

        if (!Boolean.TRUE.equals(
                reciprocidadCumplida)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Reciprocidad incumplida"
            );
        }

        if (Boolean.TRUE.equals(
                sobreendeudamiento)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Capacidad de pago insuficiente"
            );
        }

        if (Boolean.TRUE.equals(
                seguroProximoVencer)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Seguro próximo a vencer"
            );
        }

        if (!Boolean.TRUE.equals(
                informacionCompleta)) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Información incompleta"
            );
        }

        if (Boolean.TRUE.equals(
                tieneGarantiaReal)
                && cantidadGarantias != null
                && cantidadGarantias == 0) {

            advertencias++;
            agregarMotivo(
                    motivos,
                    "Crédito sin garantía vinculada"
            );
        }

        // -----------------------------------------------------
        // Informativas
        // -----------------------------------------------------
        if (Boolean.TRUE.equals(reestructurado)) {
            informativas++;
        }

        if (Boolean.TRUE.equals(novado)) {
            informativas++;
        }

        if (Boolean.TRUE.equals(alivio)) {
            informativas++;
        }

        if (Boolean.TRUE.equals(
                creditoRedescontado)) {
            informativas++;
        }

        if (Boolean.TRUE.equals(
                viviendaVis)) {
            informativas++;
        }

        if (Boolean.TRUE.equals(
                tieneSubsidio)) {
            informativas++;
        }

        if (Boolean.TRUE.equals(
                garantiaIdonea)) {
            informativas++;
        }

        this.alertasCriticas =
                criticas;

        this.alertasAdvertencia =
                advertencias;

        this.alertasInformativas =
                informativas;

        this.cantidadAlertas =
                criticas
                        + advertencias
                        + informativas;

        this.requiereActualizacionGarantias =
                Boolean.TRUE.equals(
                        garantiaInsuficiente)
                        || (
                        Boolean.TRUE.equals(
                                tieneGarantiaReal)
                                && cantidadGarantias != null
                                && cantidadGarantias == 0
                );

        this.requiereRevision =
                criticas > 0
                        || advertencias > 0;

        this.motivoRevision =
                motivos.toString();

        if (criticas > 0) {

            this.nivelAlerta =
                    "CRITICA";

            this.nivelRiesgo =
                    "ALTO";

            this.colorRiesgo =
                    "ROJO";

            this.resumenAlertas =
                    "El crédito presenta situaciones de atención prioritaria.";

        } else if (advertencias > 0) {

            this.nivelAlerta =
                    "ADVERTENCIA";

            this.nivelRiesgo =
                    "MEDIO";

            this.colorRiesgo =
                    "AMARILLO";

            this.resumenAlertas =
                    "El crédito requiere seguimiento o actualización.";

        } else if (informativas > 0) {

            this.nivelAlerta =
                    "INFORMATIVA";

            this.nivelRiesgo =
                    "BAJO";

            this.colorRiesgo =
                    "VERDE";

            this.resumenAlertas =
                    "El crédito presenta condiciones relevantes para consulta.";

        } else {

            this.nivelAlerta =
                    "NORMAL";

            this.nivelRiesgo =
                    "BAJO";

            this.colorRiesgo =
                    "VERDE";

            this.resumenAlertas =
                    "El crédito no presenta novedades gerenciales.";
        }
    }

    // =========================================================
    // Métodos privados
    // =========================================================

    private BigDecimal valorSeguro(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private boolean tieneTexto(
            String valor
    ) {

        return valor != null
                && !valor.isBlank();
    }

    private int convertirEnteroSeguro(
            long valor
    ) {

        if (valor > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        if (valor < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        return (int) valor;
    }

    private void agregarMotivo(
            StringBuilder resultado,
            String motivo
    ) {

        if (!tieneTexto(motivo)) {
            return;
        }

        if (!resultado.isEmpty()) {
            resultado.append("; ");
        }

        resultado.append(motivo);
    }
}