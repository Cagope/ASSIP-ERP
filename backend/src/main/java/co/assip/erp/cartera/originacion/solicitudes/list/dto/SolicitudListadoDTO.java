package co.assip.erp.cartera.originacion.solicitudes.list.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class SolicitudListadoDTO {

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
    // PROCESO
    // =========================================================

    private Integer idSolicitudProceso;
    private String nombreProceso;


    // =========================================================
    // RESULTADO
    // =========================================================

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
    // ESTADO
    // =========================================================

    private Boolean activo;
}