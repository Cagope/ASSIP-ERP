package co.assip.erp.cdat.analisis.tasascondiciones.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AnalisisTasasCdatResumenDTO {

    private Integer cantidadCdats;

    private Integer cantidadDepositantes;

    private BigDecimal saldoTotal;

    private BigDecimal saldoPromedio;

    private BigDecimal tasaNominalPonderada;

    private BigDecimal tasaEfectivaPonderada;

    private BigDecimal plazoPonderadoMeses;
}