
package co.assip.erp.tesoreria.autorizacion_comprobantes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Resumen de comprobantes de Tesorería.
 *
 * Utilizado para:
 * - Bandeja de autorización.
 * - Consulta de comprobantes pendientes.
 * - Historial de comprobantes.
 *
 * El consecutivo contable se asigna únicamente
 * cuando el comprobante es autorizado.
 */
public record ComprobanteResumenDTO(

        Long idComprobante,

        Integer idAgencia,

        String tipoComprobante,

        String numeroComprobante,

        String procesoOrigen,

        LocalDate fechaDocumento,

        LocalDate fechaContable,

        String concepto,

        String documentoSoporte,

        BigDecimal totalDebito,

        BigDecimal totalCredito,

        String estado,

        Integer idUsuarioElaboracion,

        LocalDateTime fechaElaboracion,

        Integer idUsuarioAutorizacion,

        LocalDateTime fechaAutorizacion

) {
}
