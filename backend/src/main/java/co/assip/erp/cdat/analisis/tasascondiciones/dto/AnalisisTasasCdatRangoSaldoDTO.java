package co.assip.erp.cdat.analisis.tasascondiciones.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AnalisisTasasCdatRangoSaldoDTO {

    private String rangoSaldo;

    private Integer cantidadCdats;

    private BigDecimal saldoTotal;

    private BigDecimal participacionSaldo;

    private BigDecimal tasaNominalPonderada;

    private BigDecimal tasaEfectivaPonderada;

    private BigDecimal tasaEfectivaMinima;

    private BigDecimal tasaEfectivaMaxima;
}