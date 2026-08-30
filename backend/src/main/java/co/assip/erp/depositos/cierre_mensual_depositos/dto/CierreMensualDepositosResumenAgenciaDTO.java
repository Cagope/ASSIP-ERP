package co.assip.erp.depositos.cierre_mensual_depositos.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CierreMensualDepositosResumenAgenciaDTO {

    private Integer idAgencia;

    private Integer totalCuentas;

    private BigDecimal saldoTotal;

    private BigDecimal totalDebitos;

    private BigDecimal totalCreditos;

    private Integer hombres;

    private Integer mujeres;

    private Integer juridicas;
}