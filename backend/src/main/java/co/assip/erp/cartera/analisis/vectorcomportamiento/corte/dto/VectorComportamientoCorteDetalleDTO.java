package co.assip.erp.cartera.analisis.vectorcomportamiento.corte.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class VectorComportamientoCorteDetalleDTO {

    // =========================================================
    // Posición dentro del Vector
    // =========================================================

    /**
     * Posición del registro dentro del Vector.
     *
     * 1      = corte seleccionado tratado como ACTUAL.
     * 2..13  = hasta 12 cierres anteriores,
     *          ordenados del más reciente al más antiguo.
     */
    private Long posicionVector;

    /**
     * Tipo de posición:
     *
     * ACTUAL = corte seleccionado.
     * CIERRE = cierre histórico anterior.
     */
    private String tipoPosicion;

    /**
     * Identificador visual del período.
     *
     * ACTUAL para la posición 1.
     * YYYY-MM para los cierres anteriores.
     */
    private String periodoVector;

    /**
     * Fecha real correspondiente a la posición.
     *
     * En posición 1 corresponde a la fecha
     * de corte seleccionada.
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
    // Corte seleccionado
    // =========================================================

    /**
     * Fecha que actúa como referencia actual
     * para construir el Vector de Comportamiento.
     */
    private LocalDate fechaCorteSeleccionado;


    // =========================================================
    // Corte correspondiente a la posición
    // =========================================================

    private Long idCierreCartera;
    private Long idCierreCarteraCredito;
    private Long idCierreCarteraResultado;

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

    private BigDecimal saldoActualFotografia;
    private BigDecimal saldoActualResultado;
    private BigDecimal saldoCreditoFechaCorte;


    // =========================================================
    // Modelo / pérdida esperada
    // =========================================================

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
}