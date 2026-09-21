package co.assip.erp.cartera.originacion.aprobacion.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class SolicitudAprobacionDecisionResponseDTO {

    private Integer idSolicitudAprobacion;
    private Integer idSolicitudCredito;
    private String numeroSolicitud;

    private Integer idEnteAprobacion;
    private String nombreEnteAprobacion;

    private Integer idAprobacionDecision;
    private String codigoDecision;
    private String nombreDecision;

    private String numeroActa;
    private LocalDate fechaActa;

    private String concepto;

    private Integer idUsuarioDecision;
    private String nombreUsuarioDecision;

    private LocalDateTime fechaDecision;

    private Integer idSolicitudProceso;
    private String nombreProceso;

    private Integer idSolicitudResultado;
    private String nombreResultado;

    private Integer idEnteActual;
}