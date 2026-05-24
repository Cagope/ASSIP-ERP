package co.assip.erp.cdat.causacion_mensual_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CdatCausacionMensualItemDTO {

    private Long idCierreMensualCdatDetalle;

    private Long idCuentaCdat;
    private String codigoCdat;

    private Integer idDatosPersonal;
    private String documento;
    private String nombreCompleto;

    private Integer idDatosPersonalCotitular;
    private String documentoCotitular;
    private String nombreCotitular;

    private LocalDate fechaAperturaCdat;
    private LocalDate fechaVencimientoCdat;
    private LocalDate fechaUltimaLiquidacion;

    private BigDecimal saldoBase;
    private BigDecimal tasaNominalAnual;

    private Integer diasCausados;

    private BigDecimal valorInteres;
    private BigDecimal valorRetencion;
    private BigDecimal valorNeto;

    private Boolean aplicaRetencion;

    private String modalidadCdat;
    private String amortizacionDeposito;

    private Integer idCuentaAportes;
    private String codigoCuentaAportes;

    private Integer idCuentaAhorro;
    private String codigoCuentaAhorro;
}