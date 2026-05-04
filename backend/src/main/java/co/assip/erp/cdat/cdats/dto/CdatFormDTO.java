package co.assip.erp.cdat.cdats.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CdatFormDTO {

    private Long idCuentaCdat;

    private Integer idAgencia;
    private Integer idProductoCdat;

    private String codigoCdat;

    private Integer idDatosPersonal;
    private Integer idDatosPersonalCotitular;

    private LocalDate fechaAperturaCdat;
    private LocalDate fechaVencimientoCdat;

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

    private LocalDate fechaUltimaLiquidacion;
    private LocalDate fechaProximaLiquidacion;

    private LocalDate fechaUltimoTrasladoInteres;
    private LocalDate fechaProximoTrasladoInteres;

    private Integer idCuentaAportes;
    private Integer idCuentaAhorro;

    private String cuentaConjunta;
    private String accionConjunta;

    private String origenCdat;
    private Long idCuentaCdatOrigen;

    private String estadoCdat;

    private String observacion;
}