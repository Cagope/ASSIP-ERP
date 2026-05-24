package co.assip.erp.cdat.liquidacion_diaria.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CdatLiquidacionDiariaEntradaDTO {

    private Integer idAgencia;

    private LocalDate fechaLiquidacion;
    private LocalDate fechaContable;

    private String tipoComprobante;
    private String numeroComprobante;

    private Boolean confirmado;
}