package co.assip.erp.cdat.catalogos.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CdatAmortizacionDTO {

    private String codigoAmortizacion;
    private String nombreAmortizacion;
    private Integer meses;
}