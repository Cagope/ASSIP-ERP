package co.assip.erp.cartera.consultacreditos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ConsultaCreditoResultadoMensualDTO {

    // =========================================================
    // Cierre
    // =========================================================

    private Integer idCierreCartera;
    private LocalDate fechaCorte;
    private String estadoCierre;

    private Integer anioCorte;
    private Integer mesCorte;
    private String periodoCorte;

    // =========================================================
    // Crédito
    // =========================================================

    private Integer idCierreCarteraCredito;
    private Integer idCarteraCredito;

    private Integer idAgencia;

    private Integer idLineaCredito;
    private String codigoLineaCredito;
    private String nombreLineaCredito;

    private String pagareCartera;

    // =========================================================
    // Estado del crédito en el corte
    // =========================================================

    private String codigoEstadoCartera;
    private String descripcionEstadoCartera;

    private String codigoEstadoJuridico;
    private String descripcionEstadoJuridico;

    private String codigoClasificacionCredito;
    private String descripcionClasificacionCredito;

    // =========================================================
    // Condiciones
    // =========================================================

    private Integer plazo;

    private BigDecimal valorCuota;

    private BigDecimal tasaNominalAnual;
    private BigDecimal tasaEfectivaAnual;

    // =========================================================
    // Fechas
    // =========================================================

    private LocalDate fechaFinal;

    private LocalDate ultimaFechaCapital;
    private LocalDate ultimaFechaInteres;

    private LocalDate proximaFechaCapital;
    private LocalDate proximaFechaInteres;

    // =========================================================
    // Resultado mensual
    // =========================================================

    private Integer idCierreCarteraResultado;

    private String codigoMetodoCalculo;

    private Boolean esUnaSolaCuota;
    private Boolean esReestructurado;

    private Integer diasMora;
    private Integer diasDiferencia;

    // =========================================================
    // Edades resultantes
    // =========================================================

    private String edadRiesgoInicialResultado;
    private String edadDeMoraResultado;
    private String edadDeRiesgoResultado;
    private String edadDePeResultado;
    private String edadDeHomologacionResultado;
    private String edadContableResultado;

    private String edadReestructuracionInicialResultado;
    private String edadReestructuradoResultado;

    // =========================================================
    // Saldos
    // =========================================================

    private BigDecimal saldoActualFotografia;
    private BigDecimal saldoActualResultado;
    private BigDecimal saldoCreditoFechaCorte;

    // =========================================================
    // Concentración del asociado
    // =========================================================

    private Integer cantidadCreditosAsociado;
    private BigDecimal saldoTotalCreditosAsociado;

    // =========================================================
    // Aportes
    // =========================================================

    private BigDecimal saldoAportesFechaCorte;
    private BigDecimal porcentajeAportesCredito;
    private BigDecimal valorAportesCredito;

    // =========================================================
    // Garantías
    // =========================================================

    private Integer cantidadBienesGarantia;
    private BigDecimal valorGarantiasTotal;
    private BigDecimal porcentajeGarantiasCredito;
    private BigDecimal valorGarantiasCredito;

    // =========================================================
    // Exposición
    // =========================================================

    private BigDecimal vea;
    private BigDecimal exposicionTotalCalculada;

    // =========================================================
    // Intereses
    // =========================================================

    private BigDecimal saldoInteresesCausados;
    private BigDecimal valorInteresesCausadosMes;

    private BigDecimal saldoInteresesContingentes;
    private BigDecimal valorInteresesContingentesMes;

    // =========================================================
    // Otros conceptos
    // =========================================================

    private BigDecimal valorCostasJudiciales;

    private BigDecimal saldoSeguros;
    private BigDecimal valorSegurosMes;

    private BigDecimal saldoAlivios;
    private BigDecimal valorAliviosMes;

    private BigDecimal valorFondosGarantias;
    private BigDecimal valorOtrosConceptos;

    // =========================================================
    // Pérdida esperada
    // =========================================================

    private BigDecimal pi;
    private BigDecimal pdi;
    private BigDecimal perdidaEsperada;

    // =========================================================
    // Deterioros
    // =========================================================

    private BigDecimal deterioroCapital;
    private BigDecimal deterioroIntereses;
    private BigDecimal deterioroOtros;
    private BigDecimal deterioroTotal;

    private BigDecimal porcentajeDeterioroCapital;

    // =========================================================
    // Cálculo
    // =========================================================

    private LocalDateTime fechaCalculo;
}