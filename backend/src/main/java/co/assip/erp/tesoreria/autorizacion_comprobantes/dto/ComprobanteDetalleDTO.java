
package co.assip.erp.tesoreria.autorizacion_comprobantes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Detalle completo de un comprobante de Tesorería.
 *
 * Utilizado para consultar un comprobante antes
 * de autorizarlo, rechazarlo o anularlo.
 *
 * Incluye:
 * - Cabecera del comprobante.
 * - Partidas contables.
 * - Movimientos físicos de caja.
 * - Información de elaboración.
 * - Información de autorización.
 * - Información de rechazo.
 * - Información de anulación.
 *
 * Los movimientos son informativos mientras
 * el comprobante permanezca PENDIENTE.
 *
 * El consecutivo y la fecha contable definitiva
 * se asignan únicamente durante la autorización.
 */
public record ComprobanteDetalleDTO(

        // ============================================
        // IDENTIFICACIÓN
        // ============================================

        Long idComprobante,

        Integer idAgencia,

        String tipoComprobante,

        String numeroComprobante,

        String procesoOrigen,

        // ============================================
        // INFORMACIÓN DEL DOCUMENTO
        // ============================================

        LocalDate fechaDocumento,

        LocalDate fechaContable,

        String concepto,

        String documentoSoporte,

        // ============================================
        // VALORES CONTABLES
        // ============================================

        BigDecimal totalDebito,

        BigDecimal totalCredito,

        // ============================================
        // ESTADO
        // ============================================

        String estado,

        // ============================================
        // ELABORACIÓN
        // ============================================

        Integer idUsuarioElaboracion,

        LocalDateTime fechaElaboracion,

        // ============================================
        // AUTORIZACIÓN
        // ============================================

        Integer idUsuarioAutorizacion,

        LocalDateTime fechaAutorizacion,

        // ============================================
        // RECHAZO
        // ============================================

        Integer idUsuarioRechazo,

        LocalDateTime fechaRechazo,

        String motivoRechazo,

        // ============================================
        // ANULACIÓN
        // ============================================

        Integer idUsuarioAnulacion,

        LocalDateTime fechaAnulacion,

        String motivoAnulacion,

        // ============================================
        // DETALLES
        // ============================================

        List<ComprobantePartidaDTO> partidasContables,

        List<ComprobanteMovimientoCajaDTO> movimientosCaja

) {

    public ComprobanteDetalleDTO {

        partidasContables = partidasContables == null
                ? List.of()
                : List.copyOf(partidasContables);

        movimientosCaja = movimientosCaja == null
                ? List.of()
                : List.copyOf(movimientosCaja);
    }
}
