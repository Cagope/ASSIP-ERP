
package co.assip.erp.contabilidad.informes.especiales.libro_diario_resumido.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Respuesta de la consulta del libro diario resumido.
 *
 * Incluye los filtros aplicados, los movimientos agrupados
 * y los totales generales de débitos y créditos.
 *
 * Los resultados corresponden exclusivamente a movimientos
 * contables activos de la agencia y período autorizados.
 */
public record LibroDiarioResumidoResponseDTO(
        Integer idAgencia,
        LocalDate fechaInicial,
        LocalDate fechaFinal,
        String modalidad,
        List<LibroDiarioResumidoFilaDTO> filas,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos,
        BigDecimal diferencia
) {
}
