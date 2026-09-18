package co.assip.erp.cartera.originacion.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudEnteAprobadorPreviewDTO {

    private Integer idEnteAprobacion;
    private String nombreEnteAprobacion;

    private String motivoAprobacion;

    private BigDecimal valorSolicitado;
    private BigDecimal valorSmmlvAplicado;
    private BigDecimal cantidadSmmlvSolicitada;

    private String codigoGarantiaCredito;
    private String descripcionGarantiaCredito;
    private String tipoGarantia;
    private String nombreTipoGarantia;

    private Integer plazoSolicitado;

    private Boolean esDirectivo;
    private Boolean esPrivilegiado;

    private String nombreTipoDirectivo;
    private String nombreCalidadDirectivo;

    private String nombreParentesco;
    private String documentoDirectivo;
    private String nombreDirectivo;

    private BigDecimal valorMinimoSmmlv;
    private BigDecimal valorTopeSmmlv;
    private BigDecimal valorTopePesos;

    private String mensajeAprobacion;
    private String detalleAprobacion;
    private String justificacionEnteAprobacion;
}