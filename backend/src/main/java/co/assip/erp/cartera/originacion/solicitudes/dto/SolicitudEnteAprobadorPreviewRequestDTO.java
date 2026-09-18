package co.assip.erp.cartera.originacion.solicitudes.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SolicitudEnteAprobadorPreviewRequestDTO {

    private Integer idAgencia;

    private Integer idDatosPersonal;

    private String codigoGarantiaCredito;

    private Integer plazoSolicitado;

    private BigDecimal valorSolicitado;
}