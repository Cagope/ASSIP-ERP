package co.assip.erp.cartera.originacion.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudFinalizarRequestDTO {

    // =========================================================
    // SOLICITUD
    // =========================================================

    private Integer idSolicitudCredito;


    // =========================================================
    // RESULTADO FINAL
    // =========================================================

    /**
     * Resultado definitivo de la solicitud.
     *
     * Para esta operación inicialmente se admitirán:
     *
     * 3 = NO VIABLE
     * 4 = DESISTIDA
     *
     * La validación corresponde al Service.
     */
    private Integer idSolicitudResultado;


    // =========================================================
    // OBSERVACIÓN FINAL
    // =========================================================

    /**
     * Comentario final con el cual se cierra la solicitud.
     */
    private String observacionFinal;
}