package co.assip.erp.cartera.originacion.aprobacion.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SolicitudAprobacionDetalleDTO {

    // =========================================================
    // Solicitud
    // =========================================================

    private Integer idSolicitudCredito;
    private String numeroSolicitud;

    private LocalDateTime fechaInicioSolicitud;
    private LocalDateTime fechaUltimaGestion;

    // =========================================================
    // Agencia
    // =========================================================

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    // =========================================================
    // Asociado principal
    // =========================================================

    private Integer idDatosPersonal;
    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    // =========================================================
    // Crédito solicitado
    // =========================================================

    private Integer idLineaCredito;
    private String codigoLineaCredito;
    private String nombreLineaCredito;

    private BigDecimal valorSolicitado;
    private Integer plazoSolicitado;
    private BigDecimal tasaColocacionAplicada;
    private BigDecimal valorCuotaProyectada;

    // =========================================================
    // Garantía
    // =========================================================

    private String codigoGarantiaCredito;
    private String nombreGarantiaCredito;
    private String tipoGarantia;

    // =========================================================
    // Estado de la solicitud
    // =========================================================

    private Integer idSolicitudProceso;
    private String nombreProceso;

    private Integer idSolicitudResultado;
    private String nombreResultado;

    // =========================================================
    // Ente aprobador
    // =========================================================

    private Integer idEnteFinal;
    private String nombreEnteFinal;

    private Integer idEnteActual;
    private String nombreEnteActual;

    // =========================================================
    // Concepto enviado por el asesor
    // =========================================================

    private String conceptoAsesorAprobacion;

    // =========================================================
    // Histórico de actuaciones
    // =========================================================

    private List<SolicitudAprobacionActuacionDTO> actuaciones;
}