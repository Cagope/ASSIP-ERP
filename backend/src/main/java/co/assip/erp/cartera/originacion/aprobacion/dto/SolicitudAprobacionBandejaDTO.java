package co.assip.erp.cartera.originacion.aprobacion.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class SolicitudAprobacionBandejaDTO {

    private Integer idSolicitudCredito;
    private String numeroSolicitud;

    private LocalDateTime fechaInicioSolicitud;
    private LocalDateTime fechaUltimaGestion;

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    private Integer idDatosPersonal;
    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    private Integer idLineaCredito;
    private String codigoLineaCredito;
    private String nombreLineaCredito;

    private BigDecimal valorSolicitado;
    private Integer plazoSolicitado;
    private BigDecimal tasaColocacionAplicada;
    private BigDecimal valorCuotaProyectada;

    private String codigoGarantiaCredito;
    private String nombreGarantiaCredito;
    private String tipoGarantia;

    private Integer idEnteFinal;
    private String nombreEnteFinal;

    private Integer idEnteActual;
}