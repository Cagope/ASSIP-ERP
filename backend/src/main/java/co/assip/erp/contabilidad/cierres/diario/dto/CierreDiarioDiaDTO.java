package co.assip.erp.contabilidad.cierres.diario.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa el estado y los movimientos de un día
 * dentro de la consulta mensual de cierres contables.
 */
public record CierreDiarioDiaDTO(
        LocalDate fecha,
        String estado,
        long cantidadComprobantes,
        long cantidadMovimientos,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos,
        BigDecimal diferencia,
        boolean tieneMovimientos,
        boolean cuadrado,
        String advertencia
) {
}