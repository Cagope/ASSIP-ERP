package co.assip.erp.cdat.analisis.concentracion.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ConcentracionCdatResumenDTO {

    private LocalDate fechaCorte;

    private Integer totalCdats;
    private Integer totalDepositantes;

    private BigDecimal saldoTotal;
    private BigDecimal saldoPromedio;

    private BigDecimal tasaPonderada;
    private BigDecimal plazoPonderadoMeses;

    private BigDecimal participacionTop10;
    private BigDecimal participacionTop20;
    private BigDecimal participacionTop50;

    private BigDecimal hhi;

    private Integer depositantesConcentran50;
    private Integer depositantesConcentran80;
}