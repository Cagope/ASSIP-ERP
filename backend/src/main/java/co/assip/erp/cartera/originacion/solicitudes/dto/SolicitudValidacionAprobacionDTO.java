package co.assip.erp.cartera.originacion.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudValidacionAprobacionDTO {

    // =========================================================
    // SOLICITUD
    // =========================================================

    private Integer idSolicitudCredito;
    private String numeroSolicitud;

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private boolean solicitudValida;
    private boolean deudoresCompletos;
    private boolean bienesCompletos;
    private boolean financieroCompleto;
    private boolean centralRiesgoCompleta;
    private boolean analisisCompleto;
    private boolean enteAprobadorDefinido;

    // =========================================================
    // RESULTADO GENERAL
    // =========================================================

    private boolean puedeEnviarAprobacion;
    private String mensaje;

}