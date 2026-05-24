package co.assip.erp.depositos.informes.saldos_rangos_edad.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class SaldosRangosEdadResumenDTO {

    private String nombreRango;
    private Integer edadInicial;
    private Integer edadFinal;

    private Integer cantidadAsociados;
    private BigDecimal saldoTotalAportes;
    private BigDecimal saldoPromedioAportes;
    private BigDecimal salarioPromedio;

}