package co.assip.erp.cdat.cierre_mensual_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CdatCierreMensualItemDTO {

    private Long idCuentaCdat;
    private String codigoCdat;

    private Integer idAgencia;

    private Integer idDatosPersonal;
    private String documento;
    private String nombreCompleto;

    private Integer idDatosPersonalCotitular;

    private String documentoCotitular;
    private String nombreCotitular;

    private LocalDate fechaAperturaCdat;
    private LocalDate fechaVencimientoCdat;

    private LocalDate fechaUltimaLiquidacion;
    private LocalDate fechaProximaLiquidacion;

    private LocalDate fechaUltimoTrasladoInteres;
    private LocalDate fechaProximoTrasladoInteres;

    private Integer plazoMeses;
    private Integer plazoDias;

    private BigDecimal valorAperturaCdat;
    private BigDecimal saldoActualCdat;

    private BigDecimal tasaNominalAnual;
    private BigDecimal tasaEfectivaAnual;

    private BigDecimal tasaNominalMensual;
    private BigDecimal tasaEfectivaMensual;

    private Boolean retencionFuenteCdat;

    private String amortizacionDeposito;
    private String modalidadCdat;

    private Integer idCuentaAportes;
    private String codigoCuentaAportes;

    private Integer idCuentaAhorro;
    private String codigoCuentaAhorro;

    private Boolean cuentaConjunta;
    private String accionConjunta;

    private String origenCdat;

    private Long idCuentaCdatOrigen;

    private String estadoCdat;
}