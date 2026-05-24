package co.assip.erp.cdat.liquidacion_diaria.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CdatLiquidacionDiariaItemDTO {

    private Long idCuentaCdat;

    private Integer idAgencia;

    private String codigoCdat;

    private Integer idDatosPersonal;

    private String documento;
    private String nombreCompleto;

    private LocalDate fechaLiquidacion;
    private LocalDate fechaProximoTraslado;

    private Integer plazoMeses;

    private BigDecimal capitalCdat;

    private BigDecimal tasaNominalAnual;

    private BigDecimal interesDiario;

    private BigDecimal retencion;

    private BigDecimal interesNeto;

    private Boolean aplicaRetencion;

    private Boolean trasladarADepositos;

    private BigDecimal valorTrasladoDepositos;

    private Integer idCuentaAhorro;

    private String codigoCuentaAhorro;

    private String codigoFormaAhorro;
    private String nombreFormaAhorro;

}