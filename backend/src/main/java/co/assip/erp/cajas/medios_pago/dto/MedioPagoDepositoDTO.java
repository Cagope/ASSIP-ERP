package co.assip.erp.cajas.medios_pago.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MedioPagoDepositoDTO {

    private Integer idCuentaAhorro;
    private String codigoCuenta;
    private BigDecimal saldoDisponible;
    private BigDecimal valorDebitar;
}