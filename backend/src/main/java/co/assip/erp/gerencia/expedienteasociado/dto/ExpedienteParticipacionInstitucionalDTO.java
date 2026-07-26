package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ExpedienteParticipacionInstitucionalDTO {

    // =========================================================
    // Tipo general de participación
    //
    // Valores esperados:
    // - DIRECTIVO
    // - COMITE
    // - PRIVILEGIADO
    // - PERSONA_RELACIONADA
    // =========================================================
    private String tipoParticipacion;

    // =========================================================
    // Indica si la participación corresponde a una persona
    // clasificada como privilegiada
    // =========================================================
    private Boolean esPrivilegiado;

    // =========================================================
    // Información de directivos
    // =========================================================
    private Long idDirectivo;

    private String codigoTipoDirectivo;
    private String nombreTipoDirectivo;

    private String calidadDirectivo;
    private String nombreCalidadDirectivo;

    private String estadoDirectivo;
    private String nombreEstadoDirectivo;

    private String actaAsamblea;
    private LocalDate fechaAsamblea;

    private String resolucionSes;
    private LocalDate fechaResolucion;

    private LocalDate fechaRetiro;

    // =========================================================
    // Información de comités
    // =========================================================
    private Long idComite;
    private String nombreComite;

    private Long idComiteDetalle;

    private String codigoCargoComite;
    private String nombreCargoComite;

    private String numeroActa;
    private LocalDate fechaNombramiento;

    // =========================================================
    // Información de privilegiados
    // =========================================================
    private Long idPrivilegiado;

    // =========================================================
    // Personas relacionadas con privilegiados
    // =========================================================
    private Long idPersonaRelacionada;

    private String documentoRelacionado;
    private String nombreRelacionado;

    private String codigoParentesco;
    private String nombreParentesco;

    // =========================================================
    // Orden de presentación
    //
    // Valores sugeridos:
    // 10 = Directivos
    // 20 = Comités
    // 30 = Privilegiados
    // 40 = Personas relacionadas
    // =========================================================
    private Integer orden;

    public ExpedienteParticipacionInstitucionalDTO() {
    }
}