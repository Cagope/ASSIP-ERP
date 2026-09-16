package co.assip.erp.cartera.originacion.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCreditoResumenDTO {

    // =========================================================
    // SOLICITUD
    // =========================================================

    private Integer idSolicitudCredito;

    private String numeroSolicitud;


    // =========================================================
    // AGENCIA
    // =========================================================

    private Integer idAgencia;

    private String nombreAgencia;


    // =========================================================
    // ASOCIADO
    // =========================================================

    private Integer idDatosPersonal;

    private String tipoDocumento;

    private String documento;

    private String nombreCompleto;


    // =========================================================
    // CRÉDITO
    // =========================================================

    private Integer idLineaCredito;

    private String nombreLineaCredito;

    private BigDecimal valorSolicitado;

    private Integer plazoSolicitado;


    // =========================================================
    // ESTADO DEL PROCESO
    // =========================================================

    private Integer idSolicitudProceso;

    private String nombreProceso;

    private Integer idSolicitudResultado;

    private String nombreResultado;

    private Boolean resultadoFinal;


    // =========================================================
    // FECHAS
    // =========================================================

    private LocalDateTime fechaInicioSolicitud;

    private LocalDateTime fechaUltimaGestion;


    // =========================================================
    // CONTROL
    // =========================================================

    private Boolean activo;
}