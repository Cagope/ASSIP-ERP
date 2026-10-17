
package co.assip.erp.tesoreria.operaciones_pendientes.dto;

import java.math.BigDecimal;

/**
 * Representa una línea de distribución del pago
 * de una obligación pendiente de Tesorería.
 *
 * Permite pagar mediante diferentes cuentas bancarias,
 * transferencias y cheques.
 */
public record OperacionPagoLineaDTO(

        Integer idBeneficiario,

        Long idCuentaBancaria,

        String medioPago,

        String numeroDocumento,

        BigDecimal valor,

        String observaciones

) {
}
