package co.assip.erp.depositos.interesmensual_tac.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InteresMensualTACItemDTO {

    private Integer idCuentaAhorro;
    private String codigoCuenta;

    private String documento;
    private String nombreCompleto;

    private BigDecimal saldoAnterior;
    private BigDecimal promedioMensual;

    private BigDecimal interesBruto;
    private BigDecimal retencion;
    private BigDecimal interesNeto;

    private BigDecimal nuevoSaldo;

    private BigDecimal tasa;
    private BigDecimal saldoActual;

    private BigDecimal tasaInteres;
    private Integer tiempoLiquidacion;
    private BigDecimal minimoForma;

    private Boolean aplicaRetencion;

    private Integer idDatosPersonal;
}