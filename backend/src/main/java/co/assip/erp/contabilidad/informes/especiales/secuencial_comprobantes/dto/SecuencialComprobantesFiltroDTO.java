
package co.assip.erp.contabilidad.informes.especiales.secuencial_comprobantes.dto;

import java.time.LocalDate;

/**
 * Filtros para consultar el secuencial de comprobantes.
 * La agencia debe estar autorizada para el usuario de sesión.
 */
public record SecuencialComprobantesFiltroDTO(
        Integer idAgencia,
        LocalDate fechaInicial,
        LocalDate fechaFinal
) {
}
