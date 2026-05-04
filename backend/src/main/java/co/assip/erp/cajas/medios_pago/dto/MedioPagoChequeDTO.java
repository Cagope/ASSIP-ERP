package co.assip.erp.cajas.medios_pago.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MedioPagoChequeDTO {

    private String codigoBanco;
    private String numeroCheque;
    private BigDecimal valorCheque;
}