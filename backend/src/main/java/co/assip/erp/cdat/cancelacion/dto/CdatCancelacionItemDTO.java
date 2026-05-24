package co.assip.erp.cdat.cancelacion.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CdatCancelacionItemDTO {

    private Long idCuentaCdat;
    private String codigoCdat;

    private Integer idAgencia;
    private Integer idDatosPersonal;

    private String documento;
    private String nombreCompleto;

    private String estadoCdat;

    private LocalDate fechaAperturaCdat;
    private LocalDate fechaVencimientoCdat;
    private LocalDate fechaUltimaLiquidacion;

    private Integer plazoMeses;
    private Integer plazoDias;

    private BigDecimal valorCapital;
    private BigDecimal saldoActualCdat;

    private BigDecimal tasaNominalAnual;
    private BigDecimal tasaEfectivaAnual;

    private Integer idCuentaAhorro;
    private Boolean retencionFuenteCdat;
    private String amortizacionDeposito;

    private BigDecimal valorInteresCausado;
    private BigDecimal valorInteresCorriente;
    private BigDecimal valorRetencion;

    private BigDecimal valorDisponible;
}