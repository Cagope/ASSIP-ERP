package co.assip.erp.cartera.analisis.concentracioncartera.dto;

import java.math.BigDecimal;

public record ConcentracionSegmentoDTO(
        Long idSegmento,
        String codigo,
        String descripcion,
        Long cantidadCreditos,
        Long cantidadDeudores,
        BigDecimal saldo,
        BigDecimal porcentajeCartera,
        BigDecimal deterioro,
        BigDecimal porcentajeDeterioro,
        BigDecimal brechaDeterioro,
        BigDecimal saldoPromedio,
        BigDecimal mayorCredito
) {
}
