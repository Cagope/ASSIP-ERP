package co.assip.erp.cartera.analisis.vectorcomportamiento.actual.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class VectorComportamientoDetalleDTO {

    // =========================================================
    // Posición dentro del Vector de Comportamiento
    // =========================================================

    /**
     * Posición dentro del vector.
     *
     * 1  = ACTUAL
     * 2  = cierre histórico más reciente
     * ...
     * 13 = cierre histórico más antiguo incluido
     */
    private Long posicionVector;

    /**
     * Tipo de posición:
     *
     * ACTUAL
     * CIERRE
     */
    private String tipoPosicion;

    /**
     * Período representado por la posición.
     *
     * Para ACTUAL:
     * ACTUAL
     *
     * Para CIERRE:
     * corresponde al período del cierre.
     */
    private String periodoVector;

    /**
     * Fecha de referencia de la posición.
     *
     * Para ACTUAL:
     * fecha actual.
     *
     * Para CIERRE:
     * fecha de corte.
     */
    private LocalDate fechaReferencia;


    // =========================================================
    // Identificación
    // =========================================================

    private Integer idCarteraCredito;

    private Integer idAgencia;

    private Integer idLineaCredito;
    private String codigoLineaCredito;
    private String nombreLineaCredito;

    private String pagareCartera;

    private Integer idDatosPersonal;

    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;


    // =========================================================
    // Corte histórico
    // =========================================================

    /**
     * Estos identificadores solamente tienen valor
     * cuando tipoPosicion = CIERRE.
     *
     * Para ACTUAL permanecen nulos.
     */
    private Long idCierreCartera;
    private Long idCierreCarteraCredito;
    private Long idCierreCarteraResultado;

    /**
     * Fecha del cierre histórico.
     *
     * Para ACTUAL permanece nula.
     */
    private LocalDate fechaCorte;

    private Integer anioCorte;
    private Integer mesCorte;

    private String periodoCorte;


    // =========================================================
    // Estado del crédito en la posición
    // =========================================================

    private String codigoEstadoCartera;
    private String descripcionEstadoCartera;

    private String codigoEstadoJuridico;
    private String descripcionEstadoJuridico;

    private String codigoClasificacionCredito;
    private String descripcionClasificacionCredito;


    // =========================================================
    // Mora
    // =========================================================

    /**
     * Para ACTUAL:
     * mora calculada con el estado actual del crédito.
     *
     * Para CIERRE:
     * mora registrada para el cierre correspondiente.
     */
    private Integer diasMora;

    /**
     * Rangos:
     *
     * AL DIA
     * MORA 1-30
     * MORA 31-60
     * MORA 61-90
     * MORA 91-120
     * MORA 121-150
     * MORA 151-180
     * MORA 181-360
     * MORA > 360
     */
    private String rangoMora;

    private Integer ordenRangoMora;

    private Boolean tieneMora;


    // =========================================================
    // Edades
    // =========================================================

    private String edadRiesgoInicialResultado;
    private String edadDeMoraResultado;
    private String edadDeRiesgoResultado;
    private String edadDePeResultado;
    private String edadDeHomologacionResultado;
    private String edadContableResultado;


    // =========================================================
    // Originación
    // =========================================================

    private BigDecimal valorInicialCredito;
    private BigDecimal valorDesembolsado;


    // =========================================================
    // Saldos
    // =========================================================

    /**
     * Valores propios de fotografía / resultado de cierre.
     *
     * Para ACTUAL pueden permanecer nulos.
     */
    private BigDecimal saldoActualFotografia;
    private BigDecimal saldoActualResultado;

    /**
     * Para CIERRE:
     * saldo del crédito en la fecha del corte.
     *
     * Para ACTUAL:
     * saldo actual del maestro.
     */
    private BigDecimal saldoCreditoFechaCorte;


    // =========================================================
    // Modelo / pérdida esperada
    // =========================================================

    /**
     * Información propia del cálculo del cierre.
     *
     * Para ACTUAL puede permanecer nula.
     */
    private String codigoMetodoCalculo;

    private BigDecimal vea;
    private BigDecimal pi;
    private BigDecimal pdi;
    private BigDecimal perdidaEsperada;


    // =========================================================
    // Deterioro
    // =========================================================

    private BigDecimal deterioroCapital;
    private BigDecimal deterioroIntereses;
    private BigDecimal deterioroOtros;
    private BigDecimal deterioroTotal;


    // =========================================================
    // Aportes
    // =========================================================

    private BigDecimal saldoAportesFechaCorte;
    private BigDecimal porcentajeAportesCredito;
    private BigDecimal valorAportesCredito;


    // =========================================================
    // Garantías
    // =========================================================

    private Long cantidadBienesGarantia;

    private BigDecimal valorGarantiasTotal;
    private BigDecimal porcentajeGarantiasCredito;
    private BigDecimal valorGarantiasCredito;


    // =========================================================
    // Estado actual del maestro
    // =========================================================

    /**
     * Estos campos corresponden al estado actual del crédito
     * en cartera.carteras_creditos.
     *
     * Se incluyen tanto en ACTUAL como en las posiciones
     * históricas para permitir contrastar cada cierre contra
     * la situación vigente del crédito.
     */
    private BigDecimal saldoActualMaestro;

    private String codigoEstadoCarteraActual;

    private Boolean creditoActivoActual;
}