package co.assip.erp.cartera.consultacreditos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ConsultaCreditoProrrogaDTO {

    // =========================================================
    // IDENTIFICACIÓN
    // =========================================================

    private Long idCreditoProrroga;

    private Integer idCarteraCredito;

    private Integer numeroProrroga;

    private LocalDate fechaProrroga;

    private Integer diasProrroga;

    private Integer mesesProrroga;

    // =========================================================
    // SITUACIÓN ANTERIOR
    // =========================================================

    private LocalDate proximaFechaCapitalAnterior;

    private LocalDate proximaFechaInteresAnterior;

    private LocalDate fechaVencimientoAnterior;

    private Integer plazoAnterior;

    // =========================================================
    // SITUACIÓN NUEVA
    // =========================================================

    private LocalDate proximaFechaCapitalNueva;

    private LocalDate proximaFechaInteresNueva;

    private LocalDate fechaVencimientoNueva;

    private Integer plazoNuevo;

    // =========================================================
    // VALORES LIQUIDADOS
    // =========================================================

    private BigDecimal valorInteresCorriente;

    private BigDecimal valorInteresMora;

    private BigDecimal valorSeguro;

    private BigDecimal valorAportes;

    private BigDecimal valorFondoGarantia;

    private BigDecimal valorOtrosConceptos;

    private BigDecimal valorTotalLiquidado;

    // =========================================================
    // VALORES PAGADOS
    // =========================================================

    private BigDecimal valorInteresCorrientePagado;

    private BigDecimal valorInteresMoraPagado;

    private BigDecimal valorSeguroPagado;

    private BigDecimal valorAportesPagado;

    private BigDecimal valorFondoGarantiaPagado;

    private BigDecimal valorOtrosConceptosPagado;

    private BigDecimal valorTotalPagado;

    // =========================================================
    // COMPROBANTE
    // =========================================================

    private String tipoComprobante;

    private String numeroComprobante;

    private LocalDate fechaComprobante;

    private Integer idAgencia;

    // =========================================================
    // ESTADO
    // =========================================================

    private String estadoProrroga;

    private LocalDateTime fechaAplicacion;

    private LocalDateTime fechaAnulacion;

    private String motivoAnulacion;

    private String observacion;
}