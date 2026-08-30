package co.assip.erp.cartera.analisis.moratemprana.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MoraTempranaResumenDTO {

    private LocalDate cosechaDesde;
    private LocalDate cosechaHasta;

    private Integer cantidadCosechas;

    private Integer creditosOriginados;
    private BigDecimal valorDesembolsado;

    private Integer mora30HastaMob3;
    private BigDecimal porcentajeMora30Mob3;

    private Integer mora30HastaMob6;
    private BigDecimal porcentajeMora30Mob6;

    private Integer mora60HastaMob6;
    private BigDecimal porcentajeMora60Mob6;
}