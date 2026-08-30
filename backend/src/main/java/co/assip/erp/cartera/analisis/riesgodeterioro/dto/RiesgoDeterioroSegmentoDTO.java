package co.assip.erp.cartera.analisis.riesgodeterioro.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RiesgoDeterioroSegmentoDTO {

    private String codigo;
    private String descripcion;

    private Integer cantidadCreditos;

    private BigDecimal saldoCartera;
    private BigDecimal porcentajeSaldo;

    private BigDecimal vea;
    private BigDecimal exposicionTotal;

    private BigDecimal perdidaEsperada;

    private BigDecimal deterioroCapital;
    private BigDecimal deterioroIntereses;
    private BigDecimal deterioroOtros;
    private BigDecimal deterioroTotal;

    private BigDecimal porcentajeDeterioroSobreSaldo;
}