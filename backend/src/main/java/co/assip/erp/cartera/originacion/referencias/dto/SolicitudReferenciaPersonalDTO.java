package co.assip.erp.cartera.referencias.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SolicitudReferenciaPersonalDTO {

    // =========================================================
    // IDENTIFICACIÓN DE LA REFERENCIA
    // =========================================================

    private Long idSolicitudReferenciaPersonal;

    // =========================================================
    // SOLICITUD Y PARTICIPANTE
    // =========================================================

    private Long idSolicitudReferenciaProceso;

    private Integer idSolicitudCredito;

    private Integer idSolicitudDeudor;

    // =========================================================
    // DATOS DE LA REFERENCIA PERSONAL
    // =========================================================

    private String nombreCompleto;

    private String telefonoCelular;

    private String telefonoFijo;

    // =========================================================
    // GESTIÓN DE LA ENTREVISTA
    // =========================================================

    private String medioEntrevista;

    private LocalDateTime fechaHoraLlamada;

    private Boolean contactoEstablecido;

    private String conceptoReferencia;

    private Integer fkSeguridadEntrevistador;

    // =========================================================
    // ESTADO
    // =========================================================

    private Boolean activo;

    // =========================================================
    // AUDITORÍA
    // =========================================================

    private Integer fkSeguridadCreacion;

    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;

    private LocalDateTime fechaEdicion;
}