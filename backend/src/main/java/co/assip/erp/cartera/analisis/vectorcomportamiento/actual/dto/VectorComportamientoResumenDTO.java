package co.assip.erp.cartera.analisis.vectorcomportamiento.actual.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class VectorComportamientoResumenDTO {

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
    // Contacto
    // =========================================================

    private String telefono;
    private String celular;
    private String correo;


    // =========================================================
    // Período observado
    // =========================================================

    private LocalDate primerCorte;
    private LocalDate ultimoCorte;

    private Integer cantidadCortesObservados;


    // =========================================================
    // Comportamiento de mora
    // =========================================================

    private Integer moraUltimoCorte;
    private Integer moraMaxima;

    private Integer cantidadCortesAlDia;
    private Integer cantidadCortesConMora;

    private Integer cantidadMora1_30;
    private Integer cantidadMora31_60;
    private Integer cantidadMora61_90;
    private Integer cantidadMora91_120;
    private Integer cantidadMora121_150;
    private Integer cantidadMora151_180;
    private Integer cantidadMora181_360;
    private Integer cantidadMoraMayor360;

    private BigDecimal pbbMora;


    // =========================================================
    // Originación
    // =========================================================

    private LocalDate fechaDesembolso;

    private BigDecimal valorInicialCredito;
    private BigDecimal valorDesembolsado;


    // =========================================================
    // Saldos y severidad
    // =========================================================

    private BigDecimal saldoPrimerCorte;
    private BigDecimal saldoUltimoCorte;

    private BigDecimal variacionSaldoPeriodo;

    private BigDecimal saldoActualMaestro;

    private BigDecimal severidad;
    private String rangoSeveridad;


    // =========================================================
    // Estado al último corte
    // =========================================================

    private String codigoEstadoCartera;
    private String descripcionEstadoCartera;

    private String codigoEstadoJuridico;
    private String descripcionEstadoJuridico;

    private String codigoClasificacionCredito;
    private String descripcionClasificacionCredito;


    // =========================================================
    // Edades al último corte
    // =========================================================

    private String edadRiesgoInicialResultado;
    private String edadDeMoraResultado;
    private String edadDeRiesgoResultado;
    private String edadDePeResultado;
    private String edadDeHomologacionResultado;
    private String edadContableResultado;


    // =========================================================
    // Modelo y deterioro
    // =========================================================

    private String codigoMetodoCalculo;

    private BigDecimal vea;
    private BigDecimal pi;
    private BigDecimal pdi;
    private BigDecimal perdidaEsperada;

    private BigDecimal deterioroCapital;
    private BigDecimal deterioroIntereses;
    private BigDecimal deterioroOtros;
    private BigDecimal deterioroTotal;


    // =========================================================
    // Estado actual del maestro
    // =========================================================

    private String codigoEstadoCarteraActual;

    private Boolean creditoActivoActual;


    // =========================================================
    // Indicadores
    // =========================================================

    private Boolean tuvoMoraPeriodo;
    private Boolean estaEnMoraUltimoCorte;
    private Boolean tuvoMoraMayor90;

    private Boolean tieneHistoria;


    // =========================================================
    // Identificadores históricos
    // =========================================================

    private Integer idCierreCartera;
    private Integer idCierreCarteraCredito;
    private Integer idCierreCarteraResultado;
}