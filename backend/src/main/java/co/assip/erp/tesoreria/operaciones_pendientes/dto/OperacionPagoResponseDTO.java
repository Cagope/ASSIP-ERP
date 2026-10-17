
package co.assip.erp.tesoreria.operaciones_pendientes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Resultado de la contabilización definitiva
 * de una obligación pendiente de Tesorería.
 *
 * Se devuelve únicamente después de registrar:
 *
 * - El comprobante contable.
 * - Las ejecuciones confirmadas.
 * - La actualización del saldo de la obligación.
 *
 * Todas estas operaciones deben realizarse
 * dentro de una misma transacción.
 */
public record OperacionPagoResponseDTO(

        Long idOperacion,

        Integer idAgencia,

        String tipoComprobante,

        String numeroComprobante,

        LocalDateTime fechaContabilizacion,

        BigDecimal valorPagado,

        BigDecimal valorEjecutadoAcumulado,

        BigDecimal saldoPendiente,

        String estadoOperacion,

        Integer cantidadLineas,

        List<Long> idEjecuciones,

        String mensaje

) {
}
