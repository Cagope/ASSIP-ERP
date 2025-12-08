package co.assip.erp.depositos.interesmensual_tac.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InteresMensualTACItemDTO {

    private Integer idCuentaAhorro;
    private String codigoCuenta;

    private String documento;
    private String nombreCompleto;

    private BigDecimal promedioMensual;

    private BigDecimal interesBruto;
    private BigDecimal retencion;
    private BigDecimal interesNeto;

    private BigDecimal tasa;               // tasa pactada por la cuenta
    private BigDecimal saldoActual;        // saldo a la fecha de liquidación

    private Boolean aplicaRetencion;       // igual que SM
}
