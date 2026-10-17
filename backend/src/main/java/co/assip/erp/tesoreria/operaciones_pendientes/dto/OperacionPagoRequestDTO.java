
package co.assip.erp.tesoreria.operaciones_pendientes.dto;

import java.util.List;

/**
 * Solicitud de pago de una obligación pendiente.
 *
 * Agrupa varias líneas de distribución para generar
 * un único comprobante contable.
 *
 * Permite:
 * - Pagos parciales.
 * - Múltiples cuentas bancarias.
 * - Transferencias y cheques.
 * - Diferentes valores por línea.
 *
 * La agencia, el usuario y las cuentas contables
 * se determinan y validan en el backend.
 */
public record OperacionPagoRequestDTO(

        Long idOperacion,

        List<OperacionPagoLineaDTO> lineas

) {
}
