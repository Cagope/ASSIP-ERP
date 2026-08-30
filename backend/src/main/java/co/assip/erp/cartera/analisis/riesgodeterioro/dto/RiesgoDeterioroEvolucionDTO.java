package co.assip.erp.cartera.analisis.riesgodeterioro.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class RiesgoDeterioroEvolucionDTO {

    private LocalDate fechaCorte;

    private Integer cantidadCreditos;

    private BigDecimal saldoCartera;
    private BigDecimal vea;
    private BigDecimal exposicionTotal;

    private BigDecimal perdidaEsperada;
    private BigDecimal deterioroTotal;

    private BigDecimal saldoMora30;
    private BigDecimal saldoMora60;
    private BigDecimal saldoMora90;
    private BigDecimal saldoMora180;

    private BigDecimal porcentajeMora30;
    private BigDecimal porcentajeMora60;
    private BigDecimal porcentajeMora90;
    private BigDecimal porcentajeMora180;

    private BigDecimal variacionSaldo;
    private BigDecimal variacionSaldoPorcentaje;

    private BigDecimal variacionPerdidaEsperada;
    private BigDecimal variacionDeterioro;
    private BigDecimal variacionDeterioroPorcentaje;
}