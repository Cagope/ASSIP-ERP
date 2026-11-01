
package co.assip.erp.contabilidad.informes.especiales.secuencial_comprobantes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa un comprobante dentro del informe secuencial.
 * Consolida los débitos y créditos de sus movimientos contables.
 */
public record SecuencialComprobantesFilaDTO(
        String tipoComprobante,
        String nombreTipoComprobante,
        String numeroComprobante,
        LocalDate fecha,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos
) {
}
