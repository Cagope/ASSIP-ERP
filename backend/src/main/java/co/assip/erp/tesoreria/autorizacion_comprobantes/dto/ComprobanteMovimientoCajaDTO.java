
package co.assip.erp.tesoreria.autorizacion_comprobantes.dto;

import java.math.BigDecimal;

/**
 * Movimiento físico de caja asociado a un comprobante
 * de Tesorería pendiente de autorización.
 *
 * Fuente:
 * tesoreria.comprobantes_movimientos_caja
 *
 * Los registros de esta tabla representan movimientos
 * proyectados y no afectan todavía la caja.
 *
 * Al autorizar el comprobante:
 *
 * 1. Se obtiene la fecha contable del servidor.
 * 2. Se valida la provisión ABIERTA de la caja.
 * 3. Se obtiene el id_provision correspondiente.
 * 4. Se registra el movimiento definitivo en
 *    cajas.movimientos_caja.
 * 5. Se vincula con el comprobante autorizado.
 *
 * Todas estas operaciones deben ejecutarse dentro
 * de la misma transacción de autorización.
 *
 * El id_provision no se almacena en este DTO,
 * porque se determina al momento de autorizar.
 */
public record ComprobanteMovimientoCajaDTO(

        // ============================================
        // IDENTIFICACIÓN
        // ============================================

        Long idComprobanteMovimientoCaja,

        Long idComprobante,

        Integer numeroLinea,

        // ============================================
        // CAJA Y OPERACIÓN
        // ============================================

        Long idCaja,

        Long idDepartamentoOperativo,

        Long idOperacionCaja,

        // ============================================
        // ORIGEN DEL MOVIMIENTO
        // ============================================

        String moduloOrigen,

        String procesoOrigen,

        // ============================================
        // DOCUMENTO DE REFERENCIA
        // ============================================

        String tipoDocumento,

        String numeroDocumento,

        String codigoReferencia,

        // ============================================
        // NATURALEZA Y MEDIO DE PAGO
        // ============================================

        String naturaleza,

        String medioPago,

        // ============================================
        // VALOR Y CONCEPTO
        // ============================================

        BigDecimal valor,

        String concepto

) {
}
