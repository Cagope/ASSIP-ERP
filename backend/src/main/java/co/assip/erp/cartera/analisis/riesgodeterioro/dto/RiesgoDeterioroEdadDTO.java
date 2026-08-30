package co.assip.erp.cartera.analisis.riesgodeterioro.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RiesgoDeterioroEdadDTO {

    private String edad;

    private Integer cantidadCreditos;

    private BigDecimal saldoCartera;
    private BigDecimal porcentajeSaldo;

    private BigDecimal vea;
    private BigDecimal exposicionTotal;

    private BigDecimal piPromedioPonderado;
    private BigDecimal pdiPromedioPonderado;

    private BigDecimal perdidaEsperada;

    private BigDecimal deterioroCapital;
    private BigDecimal deterioroIntereses;
    private BigDecimal deterioroOtros;
    private BigDecimal deterioroTotal;
}