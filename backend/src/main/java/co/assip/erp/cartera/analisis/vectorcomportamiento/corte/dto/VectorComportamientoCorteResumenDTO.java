package co.assip.erp.cartera.analisis.vectorcomportamiento.corte.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class VectorComportamientoCorteResumenDTO {

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
    // Corte seleccionado
    // =========================================================

    /**
     * Fecha seleccionada por el usuario.
     *
     * Este corte se trata como la posición actual
     * del Vector de Comportamiento por Corte.
     */
    private LocalDate fechaCorte;


    // =========================================================
    // Ventana histórica anterior
    // =========================================================

    /**
     * Corte más antiguo dentro de la ventana
     * histórica observada.
     *
     * La ventana contiene máximo 12 cierres
     * anteriores al corte seleccionado.
     */
    private LocalDate primerCorte;

    /**
     * Cierre histórico inmediatamente anterior
     * más reciente dentro de la ventana.
     *
     * No corresponde al corte seleccionado.
     */
    private LocalDate ultimoCorte;

    /**
     * Cantidad de cierres históricos anteriores
     * utilizados para construir los indicadores.
     *
     * Valor esperado:
     *
     * 0..12
     */
    private Integer cantidadCortesObservados;


    // =========================================================
    // Comportamiento de mora
    // =========================================================

    /**
     * Mora correspondiente al corte seleccionado.
     *
     * Aunque conserva el nombre histórico
     * moraUltimoCorte, representa la posición
     * de referencia del Vector.
     */
    private Integer moraUltimoCorte;

    /**
     * Mora máxima observada exclusivamente
     * en los cierres históricos anteriores.
     */
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

    /**
     * Porcentaje de cierres históricos anteriores
     * que presentaron mora.
     *
     * Máximo 12 cierres observados.
     */
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

    /**
     * Saldo del cierre histórico más antiguo
     * dentro de la ventana de máximo 12 cierres.
     */
    private BigDecimal saldoPrimerCorte;

    /**
     * Saldo del crédito en el corte seleccionado.
     */
    private BigDecimal saldoUltimoCorte;

    /**
     * Variación entre:
     *
     * saldo del corte seleccionado
     * -
     * saldo del cierre histórico más antiguo
     * de la ventana.
     */
    private BigDecimal variacionSaldoPeriodo;

    /**
     * Saldo del crédito al corte seleccionado
     * dividido por valor desembolsado.
     */
    private BigDecimal severidad;

    private String rangoSeveridad;


    // =========================================================
    // Estado en el corte seleccionado
    // =========================================================

    private String codigoEstadoCartera;
    private String descripcionEstadoCartera;

    private String codigoEstadoJuridico;
    private String descripcionEstadoJuridico;

    private String codigoClasificacionCredito;
    private String descripcionClasificacionCredito;


    // =========================================================
    // Edades en el corte seleccionado
    // =========================================================

    private String edadRiesgoInicialResultado;
    private String edadDeMoraResultado;
    private String edadDeRiesgoResultado;
    private String edadDePeResultado;
    private String edadDeHomologacionResultado;
    private String edadContableResultado;


    // =========================================================
    // Modelo y deterioro en el corte seleccionado
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
    // Indicadores
    // =========================================================

    /**
     * Indica si presentó mora en alguno
     * de los cierres históricos anteriores
     * de la ventana observada.
     */
    private Boolean tuvoMoraPeriodo;

    /**
     * Indica si el crédito estaba en mora
     * exactamente en el corte seleccionado.
     */
    private Boolean estaEnMoraUltimoCorte;

    /**
     * Indica si tuvo mora superior a 90 días
     * en alguno de los cierres históricos
     * anteriores observados.
     */
    private Boolean tuvoMoraMayor90;


    // =========================================================
    // Identificadores del corte seleccionado
    // =========================================================

    private Long idCierreCartera;
    private Long idCierreCarteraCredito;
    private Long idCierreCarteraResultado;
}