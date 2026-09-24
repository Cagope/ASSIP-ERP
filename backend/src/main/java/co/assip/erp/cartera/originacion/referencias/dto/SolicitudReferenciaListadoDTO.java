package co.assip.erp.cartera.referencias.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class SolicitudReferenciaListadoDTO {

    // =========================================================
    // SOLICITUD
    // =========================================================

    private Integer idSolicitudCredito;
    private String numeroSolicitud;

    private LocalDateTime fechaInicioSolicitud;
    private LocalDateTime fechaUltimaGestion;

    // =========================================================
    // AGENCIA
    // =========================================================

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    // =========================================================
    // SOLICITANTE
    // =========================================================

    private Integer idDatosPersonal;
    private String tipoDocumento;
    private String documento;
    private String nombreSolicitante;

    // =========================================================
    // ASESOR RESPONSABLE
    // =========================================================

    private Integer idAsesor;
    private String usuarioAsesor;
    private String nombreAsesor;

    // =========================================================
    // PROCESO GENERAL DE LA SOLICITUD
    // =========================================================

    private Integer idSolicitudProceso;
    private String nombreProceso;

    private Integer idSolicitudResultado;
    private String nombreResultado;

    // =========================================================
    // CRÉDITO
    // =========================================================

    private BigDecimal valorSolicitado;

    private Integer idLineaCredito;
    private String codigoLineaCredito;
    private String nombreLineaCredito;

    // =========================================================
    // PROCESO DE REFERENCIAS PERSONALES
    // =========================================================

    private Long idSolicitudReferenciaProceso;

    private String estadoReferencias;
    private String tipoCierre;
    private String observacionCierre;

    private LocalDateTime fechaInicioReferencias;
    private LocalDateTime fechaCierre;

    private Integer fkSeguridadCierre;

    // =========================================================
    // INDICADORES
    // =========================================================

    private Integer cantidadReferencias;
}