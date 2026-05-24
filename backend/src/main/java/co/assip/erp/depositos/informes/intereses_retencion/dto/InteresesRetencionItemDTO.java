package co.assip.erp.depositos.informes.intereses_retencion.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class InteresesRetencionItemDTO {

    private String codigoAgencia;
    private String nombreAgencia;

    private String codigoForma;
    private String nombreForma;

    private String codigoCuenta;

    private String documento;
    private String nombreCompleto;

    private BigDecimal intereses;
    private BigDecimal retencion;
    private BigDecimal neto;

}