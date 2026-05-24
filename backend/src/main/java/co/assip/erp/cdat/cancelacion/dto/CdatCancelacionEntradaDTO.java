package co.assip.erp.cdat.cancelacion.dto;

import co.assip.erp.cajas.medios_pago.dto.MediosPagoDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CdatCancelacionEntradaDTO {

    private Long idCuentaCdat;

    private Integer idAgencia;
    private Integer idDatosPersonal;

    private LocalDate fechaProceso;
    private LocalDate fechaLiquidacion;

    private String tipoComprobante;
    private String numeroComprobante;

    private BigDecimal valorRenovacion;

    private MediosPagoDTO mediosPagoEntrada;
    private MediosPagoDTO mediosPagoSalida;

    private String observacion;

    private LocalDate fechaAperturaNuevoCdat;
    private LocalDate fechaVencimientoNuevoCdat;

    private Integer plazoMesesNuevoCdat;
    private Integer plazoDiasNuevoCdat;

    private BigDecimal tasaNominalAnualNuevoCdat;
    private BigDecimal tasaEfectivaAnualNuevoCdat;

    private String amortizacionDepositoNuevoCdat;
    private Boolean retencionFuenteNuevoCdat;

    private Integer idCuentaAhorroNuevoCdat;

    private String observacionNuevoCdat;

}