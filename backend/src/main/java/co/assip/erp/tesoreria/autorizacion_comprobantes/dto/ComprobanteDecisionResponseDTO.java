
package co.assip.erp.tesoreria.autorizacion_comprobantes.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Respuesta de una decisión sobre un comprobante
 * de Tesorería.
 *
 * Se utiliza después de ejecutar:
 *
 * - AUTORIZAR
 * - RECHAZAR
 * - ANULAR
 *
 * Reglas:
 *
 * AUTORIZAR:
 * - El comprobante pasa a CONTABILIZADO.
 * - Se asigna el consecutivo definitivo.
 * - Se establece la fecha contable del servidor.
 * - Se registran los movimientos definitivos.
 *
 * RECHAZAR:
 * - El comprobante pasa a RECHAZADO.
 * - No se genera consecutivo.
 * - No se contabiliza.
 *
 * ANULAR:
 * - El comprobante pasa a ANULADO.
 * - No se genera consecutivo.
 * - No se contabiliza.
 *
 * La respuesta se construye únicamente después
 * de ejecutar satisfactoriamente la operación.
 */
public record ComprobanteDecisionResponseDTO(

        // ============================================
        // IDENTIFICACIÓN
        // ============================================

        Long idComprobante,

        Integer idAgencia,

        String tipoComprobante,

        String numeroComprobante,

        String procesoOrigen,

        // ============================================
        // RESULTADO
        // ============================================

        String estado,

        String decision,

        String mensaje,

        // ============================================
        // FECHAS
        // ============================================

        LocalDate fechaDocumento,

        LocalDate fechaContable,

        LocalDateTime fechaDecision,

        // ============================================
        // AUDITORÍA
        // ============================================

        Integer idUsuarioDecision,

        String motivo

) {

    /**
     * Construye la respuesta de una autorización exitosa.
     *
     * Este método debe utilizarse únicamente después
     * de completar la contabilización.
     */
    public static ComprobanteDecisionResponseDTO autorizado(

            Long idComprobante,
            Integer idAgencia,
            String tipoComprobante,
            String numeroComprobante,
            String procesoOrigen,
            LocalDate fechaDocumento,
            LocalDate fechaContable,
            LocalDateTime fechaAutorizacion,
            Integer idUsuarioAutorizacion

    ) {

        return new ComprobanteDecisionResponseDTO(

                idComprobante,
                idAgencia,
                tipoComprobante,
                numeroComprobante,
                procesoOrigen,

                "CONTABILIZADO",
                "AUTORIZAR",

                "Comprobante autorizado y contabilizado correctamente.",

                fechaDocumento,
                fechaContable,
                fechaAutorizacion,

                idUsuarioAutorizacion,
                null
        );
    }

    /**
     * Construye la respuesta de un rechazo.
     */
    public static ComprobanteDecisionResponseDTO rechazado(

            Long idComprobante,
            Integer idAgencia,
            String tipoComprobante,
            String procesoOrigen,
            LocalDate fechaDocumento,
            LocalDateTime fechaRechazo,
            Integer idUsuarioRechazo,
            String motivoRechazo

    ) {

        return new ComprobanteDecisionResponseDTO(

                idComprobante,
                idAgencia,
                tipoComprobante,
                null,
                procesoOrigen,

                "RECHAZADO",
                "RECHAZAR",

                "Comprobante rechazado correctamente.",

                fechaDocumento,
                null,
                fechaRechazo,

                idUsuarioRechazo,
                motivoRechazo
        );
    }

    /**
     * Construye la respuesta de una anulación.
     *
     * La anulación puede realizarla el elaborador
     * o un usuario con permiso de autorización.
     *
     * Solo aplica a comprobantes PENDIENTES.
     */
    public static ComprobanteDecisionResponseDTO anulado(

            Long idComprobante,
            Integer idAgencia,
            String tipoComprobante,
            String procesoOrigen,
            LocalDate fechaDocumento,
            LocalDateTime fechaAnulacion,
            Integer idUsuarioAnulacion,
            String motivoAnulacion

    ) {

        return new ComprobanteDecisionResponseDTO(

                idComprobante,
                idAgencia,
                tipoComprobante,
                null,
                procesoOrigen,

                "ANULADO",
                "ANULAR",

                "Comprobante anulado correctamente.",

                fechaDocumento,
                null,
                fechaAnulacion,

                idUsuarioAnulacion,
                motivoAnulacion
        );
    }
}
