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
public class SolicitudCrearRetomarResponseDTO {

    private Integer idSolicitudCredito;

    private String numeroSolicitud;

    /**
     * Valores esperados desde PostgreSQL:
     * CREADA
     * RETOMADA
     */
    private String accion;

    private Integer idSolicitudProceso;

    private String nombreProceso;

    private Integer idSolicitudResultado;

    private String nombreResultado;

    private LocalDateTime fechaUltimaGestion;
}