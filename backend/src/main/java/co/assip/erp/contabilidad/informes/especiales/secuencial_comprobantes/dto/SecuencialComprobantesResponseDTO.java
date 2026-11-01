
package co.assip.erp.contabilidad.informes.especiales.secuencial_comprobantes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Resultado del informe Secuencial de comprobantes.
 * Incluye los filtros aplicados, comprobantes consolidados
 * y totales generales del período consultado.
 * El frontend agrupa las filas por tipo de comprobante
 * para presentar los encabezados y subtotales por corte.
 */
public record SecuencialComprobantesResponseDTO(
        Integer idAgencia,
        LocalDate fechaInicial,
        LocalDate fechaFinal,
        List<SecuencialComprobantesFilaDTO> filas,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos,
        BigDecimal diferencia
) {
}
