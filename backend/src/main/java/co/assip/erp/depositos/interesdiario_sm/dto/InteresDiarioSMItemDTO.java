package co.assip.erp.depositos.interesdiario_sm.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class InteresDiarioSMItemDTO implements Serializable {

    private Integer idCuentaAhorro;
    private String codigoCuenta;

    private String documento;
    private String nombreCompleto;

    private BigDecimal saldoAnterior;
    private BigDecimal saldoMinimoDia;

    private BigDecimal interesBruto;
    private BigDecimal retencion;
    private BigDecimal interesNeto;

    private BigDecimal nuevoSaldo;

    private BigDecimal tasaInteres;
    private Integer tiempoLiquidacion;
    private BigDecimal minimoForma;

    private Boolean aplicaRetencion;

    private Integer idDatosPersonal;

}