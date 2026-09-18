package co.assip.erp.cartera.originacion.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudFinalizarResponseDTO {

    // =========================================================
    // SOLICITUD
    // =========================================================

    private Integer idSolicitudCredito;

    private String numeroSolicitud;


    // =========================================================
    // RESULTADO FINAL
    // =========================================================

    private Integer idSolicitudResultado;

    private String nombreResultado;

    private String observacionFinal;


    // =========================================================
    // GESTIÓN
    // =========================================================

    private LocalDateTime fechaUltimaGestion;
}