package co.assip.erp.depositos.informes.saldos_menores.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SaldosMenoresResumenDTO {

    private Integer totalCuentas;
    private BigDecimal totalSaldos;
    private BigDecimal saldoPromedio;
    private BigDecimal valorMaximo;

}