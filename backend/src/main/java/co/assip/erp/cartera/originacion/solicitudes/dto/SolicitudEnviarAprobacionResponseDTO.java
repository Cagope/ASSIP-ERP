package co.assip.erp.cartera.originacion.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudEnviarAprobacionResponseDTO {

    // =========================================================
    // SOLICITUD
    // =========================================================

    private Integer idSolicitudCredito;
    private String numeroSolicitud;

    // =========================================================
    // ESTADO
    // =========================================================

    private Integer idSolicitudProceso;
    private String nombreProceso;

    private Integer idSolicitudResultado;
    private String nombreResultado;

    // =========================================================
    // ENTE APROBADOR
    // =========================================================

    private Integer idEnteAprobacion;
    private String nombreEnteAprobacion;

    // =========================================================
    // GESTIÓN
    // =========================================================

    private LocalDateTime fechaUltimaGestion;

}