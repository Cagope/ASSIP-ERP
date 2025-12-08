package co.assip.erp.depositos.interesmensual_sm.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InteresMensualSMItemDTO {

    private Integer idCuentaAhorro;
    private String codigoCuenta;

    private String documento;
    private String nombreCompleto;

    private BigDecimal saldoMinimoMes;

    private BigDecimal interesBruto;
    private BigDecimal retencion;
    private BigDecimal interesNeto;

    private BigDecimal tasaInteres;
    private Integer tiempoLiquidacion;
    private BigDecimal minimoForma;

    private Boolean aplicaRetencion;
}
