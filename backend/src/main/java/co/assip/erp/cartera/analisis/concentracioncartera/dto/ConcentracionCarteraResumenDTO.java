package co.assip.erp.cartera.analisis.concentracioncartera.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConcentracionCarteraResumenDTO(
        LocalDate fechaCorte,

        Long cantidadCreditos,
        Long cantidadDeudores,
        BigDecimal saldoTotal,
        BigDecimal deterioroTotal,

        BigDecimal saldoMayorDeudor,
        BigDecimal porcentajeMayorDeudor,

        BigDecimal saldoTop10,
        BigDecimal porcentajeTop10,
        BigDecimal saldoTop20,
        BigDecimal porcentajeTop20,
        BigDecimal saldoTop50,
        BigDecimal porcentajeTop50,

        BigDecimal hhi10000,
        Long deudoresPara50Pct,
        Long deudoresPara80Pct,
        BigDecimal porcentajeDeudoresPara50Pct,
        BigDecimal porcentajeDeudoresPara80Pct,

        Long deudoresConDeterioro,
        BigDecimal deterioroMayorDeudor,
        BigDecimal porcentajeDeterioroMayorDeudor,
        BigDecimal deterioroTop10,
        BigDecimal porcentajeDeterioroTop10,
        BigDecimal deterioroTop20,
        BigDecimal porcentajeDeterioroTop20,
        BigDecimal deterioroTop50,
        BigDecimal porcentajeDeterioroTop50
) {
}
