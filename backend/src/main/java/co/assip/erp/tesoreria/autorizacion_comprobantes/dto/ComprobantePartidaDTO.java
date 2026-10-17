
package co.assip.erp.tesoreria.autorizacion_comprobantes.dto;

import java.math.BigDecimal;

/**
 * Partida contable de un comprobante de Tesorería.
 *
 * Fuente:
 * tesoreria.comprobantes_detalles
 *
 * Cada registro representa un débito o crédito
 * que será contabilizado únicamente cuando
 * el comprobante sea autorizado.
 *
 * Mientras el comprobante esté PENDIENTE,
 * estas partidas no afectan:
 *
 * - contabilidad.auxiliares_contables
 * - saldos contables
 * - movimientos definitivos
 */
public record ComprobantePartidaDTO(

        // ============================================
        // IDENTIFICACIÓN
        // ============================================

        Long idComprobanteDetalle,

        Long idComprobante,

        Integer numeroLinea,

        // ============================================
        // CUENTA CONTABLE
        // ============================================

        Integer idCatalogoCuenta,

        Integer idAgencia,

        Integer idDatosPersonal,

        // ============================================
        // DESCRIPCIÓN DEL MOVIMIENTO
        // ============================================

        String detalleMovimiento,

        // ============================================
        // VALORES
        // ============================================

        BigDecimal valorDebito,

        BigDecimal valorCredito,

        BigDecimal valorBase,

        // ============================================
        // TRAZABILIDAD
        // ============================================

        String origenModulo,

        String documentoOrigen,

        String documentoSoporte

) {
}
