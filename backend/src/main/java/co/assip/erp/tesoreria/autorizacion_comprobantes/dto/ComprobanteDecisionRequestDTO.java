
package co.assip.erp.tesoreria.autorizacion_comprobantes.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Solicitud para tomar una decisión sobre un comprobante
 * de Tesorería pendiente de autorización.
 *
 * Operaciones permitidas:
 *
 * AUTORIZAR:
 * - Requiere permiso de autorización.
 * - No permite autorizar comprobantes propios.
 * - Genera consecutivo y contabiliza.
 *
 * RECHAZAR:
 * - Requiere permiso de autorización.
 * - No permite rechazar comprobantes propios.
 * - Exige motivo.
 * - No genera consecutivo ni movimientos definitivos.
 *
 * ANULAR:
 * - Permitido al elaborador o a un usuario
 *   con permiso de autorización.
 * - Exige motivo.
 * - No genera consecutivo ni movimientos definitivos.
 *
 * El usuario y la fecha de la operación se determinan
 * en el backend.
 */
public record ComprobanteDecisionRequestDTO(

        @NotNull(message = "Debe indicar la decisión.")
        Decision decision,

        @Size(
                max = 500,
                message = "El motivo no puede superar 500 caracteres."
        )
        String motivo

) {

    /**
     * Decisiones disponibles sobre un comprobante pendiente.
     */
    public enum Decision {

        AUTORIZAR,

        RECHAZAR,

        ANULAR
    }

    /**
     * Valida los requisitos funcionales de la solicitud.
     *
     * El permiso del usuario, el estado del comprobante
     * y la separación entre elaborador y autorizador
     * se validan posteriormente en el Service.
     */
    public void validar() {

        if (decision == null) {
            throw new IllegalArgumentException(
                    "Debe indicar la decisión sobre el comprobante."
            );
        }

        if (decision == Decision.RECHAZAR
                || decision == Decision.ANULAR) {

            if (motivo == null || motivo.isBlank()) {
                throw new IllegalArgumentException(
                        "Debe indicar el motivo del rechazo o anulación."
                );
            }
        }

        if (motivo != null && motivo.length() > 500) {
            throw new IllegalArgumentException(
                    "El motivo no puede superar 500 caracteres."
            );
        }
    }
}
