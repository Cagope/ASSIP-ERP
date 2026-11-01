
package co.assip.erp.contabilidad.informes.especiales.libro_diario_resumido.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa una fila del libro diario resumido.
 *
 * La agrupación corresponde a una cuenta contable o a un
 * tipo de comprobante, según la modalidad seleccionada.
 *
 * Los valores corresponden exclusivamente a movimientos
 * contables activos de la agencia y período consultados.
 */
public record LibroDiarioResumidoFilaDTO(
        LocalDate fecha,
        String codigoAgrupacion,
        String nombreAgrupacion,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos
) {
}
