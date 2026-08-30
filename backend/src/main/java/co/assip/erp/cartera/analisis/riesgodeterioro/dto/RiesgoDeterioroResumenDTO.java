package co.assip.erp.cartera.analisis.riesgodeterioro.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class RiesgoDeterioroResumenDTO {

    private LocalDate fechaCorte;

    private Integer cantidadCreditos;

    private BigDecimal saldoCartera;
    private BigDecimal vea;
    private BigDecimal exposicionTotal;

    private BigDecimal perdidaEsperada;

    private BigDecimal deterioroCapital;
    private BigDecimal deterioroIntereses;
    private BigDecimal deterioroOtros;
    private BigDecimal deterioroTotal;

    private BigDecimal porcentajePerdidaEsperadaSobreVea;
    private BigDecimal porcentajeDeterioroSobreSaldo;

    private Integer cantidadMora30;
    private BigDecimal saldoMora30;
    private BigDecimal porcentajeSaldoMora30;

    private Integer cantidadMora60;
    private BigDecimal saldoMora60;
    private BigDecimal porcentajeSaldoMora60;

    private Integer cantidadMora90;
    private BigDecimal saldoMora90;
    private BigDecimal porcentajeSaldoMora90;

    private Integer cantidadMora180;
    private BigDecimal saldoMora180;
    private BigDecimal porcentajeSaldoMora180;
}