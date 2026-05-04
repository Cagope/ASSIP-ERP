package co.assip.erp.cajas.medios_pago.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MedioPagoTrasladoAgenciaDTO {

    private Integer idCatalogoCuentaTraslado;
    private String codigoCuentaTraslado;
    private String nombreCuentaTraslado;
    private BigDecimal valorTraslado;
}