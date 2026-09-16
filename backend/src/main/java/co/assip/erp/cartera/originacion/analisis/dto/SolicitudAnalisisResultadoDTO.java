package co.assip.erp.cartera.originacion.analisis.dto;

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
public class SolicitudAnalisisResultadoDTO {

    // =========================================================
    // SOLICITUD
    // =========================================================

    private Integer idSolicitudCredito;
    private String numeroSolicitud;

    private Integer idAgencia;
    private Integer idDatosPersonal;

    private LocalDateTime fechaInicioSolicitud;
    private LocalDateTime fechaUltimaGestion;

    private Integer idSolicitudProceso;
    private Integer idSolicitudResultado;


    // =========================================================
    // CRÉDITO
    // =========================================================

    private Integer idLineaCredito;
    private String codigoGarantiaCredito;

    private BigDecimal valorSolicitado;
    private BigDecimal valorCuotaProyectada;


    // =========================================================
    // MODELO
    // =========================================================

    private Integer idSolicitudModelo;
    private String versionModelo;

    private Integer cantidadModelosDetectados;


    // =========================================================
    // POBLACIÓN
    // =========================================================

    private Integer cantidadPersonas;
    private Integer cantidadPersonasConResultado;
    private Integer cantidadPersonasEvaluadas;
    private Integer cantidadPersonasPendientes;

    private Boolean analisisSolicitudCompleto;
    private String detallePersonasPendientes;


    // =========================================================
    // RESULTADO
    // =========================================================

    private BigDecimal puntajeReferenciaSolicitud;

    private String perfilRiesgoSolicitud;
    private String recomendacionSolicitud;

    private Boolean cumpleOtorgamientoSolicitud;

    private String estadoAnalisisSolicitud;
    private String motivoResultadoSolicitud;
}