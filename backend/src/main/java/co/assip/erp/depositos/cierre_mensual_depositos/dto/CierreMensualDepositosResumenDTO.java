package co.assip.erp.depositos.cierre_mensual_depositos.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CierreMensualDepositosResumenDTO {

    private Integer totalCuentas;

    private BigDecimal saldoTotal;

    private BigDecimal totalDebitos;

    private BigDecimal totalCreditos;

    private Integer totalFormas;

    private Integer hombres;

    private Integer mujeres;

    private Integer juridicas;

}