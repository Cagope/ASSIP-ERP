package co.assip.erp.cartera.originacion.formalizacion.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class SolicitudFormalizacionBandejaDTO {

    // =========================================================
    // IDENTIFICACIÓN DE LA SOLICITUD
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
    // LÍNEA DE CRÉDITO
    // =========================================================

    private Integer idLineaCredito;
    private String nombreLineaCredito;

    // =========================================================
    // CONDICIONES SOLICITADAS
    // =========================================================

    private BigDecimal valorSolicitado;
    private Integer plazoSolicitado;

    // =========================================================
    // CONDICIONES DEFINITIVAS
    // =========================================================

    private BigDecimal valorFormalizado;
    private Integer plazoFormalizado;

    // =========================================================
    // FECHAS DE CONTROL
    // =========================================================

    private LocalDateTime fechaFinAprobacion;
    private LocalDateTime fechaUltimaGestion;

    // =========================================================
    // CONTROL DE FORMALIZACIÓN
    // =========================================================

    private Boolean condicionesModificadas;

    // =========================================================
    // CRÉDITO Y PAGARÉ
    // =========================================================

    private Integer idCarteraCredito;
    private String pagareCartera;

    // =========================================================
    // ESTADO PARA LA BANDEJA
    // =========================================================

    private String estadoFormalizacion;

}