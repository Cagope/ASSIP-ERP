package co.assip.erp.cajas.movimientos.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ChequeRecibidoDTO {

    private String codigoBanco;
    private String numeroCheque;
    private BigDecimal valorCheque;

}