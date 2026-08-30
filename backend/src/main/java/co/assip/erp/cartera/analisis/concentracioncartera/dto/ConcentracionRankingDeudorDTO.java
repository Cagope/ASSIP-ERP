package co.assip.erp.cartera.analisis.concentracioncartera.dto;

import java.math.BigDecimal;

public record ConcentracionRankingDeudorDTO(
        Long posicion,
        Long idDatosPersonal,
        String documento,
        String nombreCompleto,
        Long cantidadCreditos,
        BigDecimal saldo,
        BigDecimal porcentajeCartera,
        BigDecimal porcentajeCarteraAcumulado,
        BigDecimal deterioro,
        BigDecimal porcentajeDeterioro,
        BigDecimal porcentajeDeterioroAcumulado
) {
}
