package co.assip.erp.cartera.referencias.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitudReferenciaParticipanteDTO {

    // =========================================================
    // SOLICITUD
    // =========================================================

    private Integer idSolicitudCredito;

    // =========================================================
    // PARTICIPANTE
    // =========================================================

    private Integer idSolicitudDeudor;
    private Integer idDatosPersonal;

    private String tipoDeudor;
    private Integer ordenDeudor;

    // =========================================================
    // IDENTIFICACIÓN
    // =========================================================

    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    // =========================================================
    // PROCESO DE REFERENCIAS
    // =========================================================

    private Long idSolicitudReferenciaProceso;
    private String estadoReferencias;

    // =========================================================
    // RESUMEN DE REFERENCIAS DEL PARTICIPANTE
    // =========================================================

    private Integer cantidadReferencias;
    private Integer cantidadContactadas;
    private Integer cantidadNoContactadas;
    private Integer cantidadPendientes;
}