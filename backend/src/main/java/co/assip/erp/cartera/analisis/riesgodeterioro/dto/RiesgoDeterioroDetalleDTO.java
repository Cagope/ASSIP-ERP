package co.assip.erp.cartera.analisis.riesgodeterioro.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class RiesgoDeterioroDetalleDTO {

    private Integer idCarteraCredito;
    private Integer idCierreCarteraCredito;

    private Integer idAgencia;

    private Integer idLineaCredito;
    private String codigoLineaCredito;
    private String nombreLineaCredito;

    private String pagareCartera;

    private Integer idDatosPersonal;
    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    private String codigoClasificacionCredito;
    private String descripcionClasificacionCredito;

    private String codigoGarantiaCredito;
    private String descripcionGarantiaCredito;
    private String tipoGarantia;

    private String codigoDestinoEconomico;
    private String descripcionDestinoEconomico;

    private String codigoEstadoCartera;
    private String descripcionEstadoCartera;

    private String codigoEstadoJuridico;
    private String descripcionEstadoJuridico;

    private String codigoModificacionCredito;
    private String descripcionModificacionCredito;

    private Boolean esReestructurado;

    private LocalDate fechaDesembolso;
    private LocalDate fechaCorte;

    private BigDecimal valorInicialCredito;
    private BigDecimal valorDesembolsado;

    private BigDecimal saldoCartera;

    private Integer diasMora;

    private String edadRiesgoInicial;
    private String edadMora;
    private String edadRiesgo;
    private String edadPe;
    private String edadHomologacion;
    private String edadContable;

    private BigDecimal saldoAportesFechaCorte;
    private BigDecimal porcentajeAportesCredito;
    private BigDecimal valorAportesCredito;

    private Integer cantidadBienesGarantia;
    private BigDecimal valorGarantiasTotal;
    private BigDecimal porcentajeGarantiasCredito;
    private BigDecimal valorGarantiasCredito;

    private BigDecimal vea;

    private BigDecimal saldoInteresesCausados;
    private BigDecimal saldoInteresesContingentes;

    private BigDecimal valorCostasJudiciales;
    private BigDecimal saldoSeguros;
    private BigDecimal saldoAlivios;
    private BigDecimal valorFondosGarantias;
    private BigDecimal valorOtrosConceptos;

    private BigDecimal pi;
    private BigDecimal pdi;

    private BigDecimal perdidaEsperada;

    private BigDecimal deterioroCapital;
    private BigDecimal deterioroIntereses;
    private BigDecimal deterioroOtros;
    private BigDecimal deterioroTotal;

    private BigDecimal exposicionTotal;
}