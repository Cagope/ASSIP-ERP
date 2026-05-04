package co.assip.erp.cajas.medios_pago.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MedioPagoBancoDTO {

    private Integer idCatalogoCuentaBanco;
    private String codigoCuentaBanco;
    private String nombreCuentaBanco;
    private BigDecimal valorBanco;
}