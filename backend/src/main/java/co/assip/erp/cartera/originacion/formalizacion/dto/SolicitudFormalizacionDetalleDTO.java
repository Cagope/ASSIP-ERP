package co.assip.erp.cartera.originacion.formalizacion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudFormalizacionDetalleDTO {

    // =========================================================
    // IDENTIFICACIÓN DE LA SOLICITUD
    // =========================================================

    private Integer idSolicitudCredito;
    private String numeroSolicitud;

    private Integer idAgencia;
    private String nombreAgencia;

    private Integer idDatosPersonal;
    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    // =========================================================
    // ESTADO DEL PROCESO
    // =========================================================

    private Integer idSolicitudProceso;
    private String nombreProceso;

    private Integer idSolicitudResultado;
    private String nombreResultado;

    // =========================================================
    // CRÉDITO SOLICITADO
    // =========================================================

    private Integer idLineaCredito;
    private String nombreLineaCredito;

    private BigDecimal valorSolicitado;
    private Integer plazoSolicitado;

    private String codigoFormaPago;
    private String periodoCodigoInteres;
    private String tipoModalidadInteres;

    private Integer amortizacionCapital;
    private String codigoTipoCuota;

    private Integer mesesGraciaCapital;
    private Integer mesesGraciaInteres;

    private BigDecimal tasaColocacionAplicada;
    private BigDecimal tasaEfectivaAnual;
    private BigDecimal valorCuotaProyectada;

    // =========================================================
    // GARANTÍAS
    // =========================================================

    private String codigoGarantiaCredito;
    private String nombreGarantiaCredito;

    private Integer idFondoGarantia;
    private String nombreFondoGarantia;

    // =========================================================
    // DECISIÓN DEL ENTE APROBADOR
    //
    // Información de consulta.
    // No corresponde a condiciones financieras formalizadas.
    // =========================================================

    private Integer idEnteAprobacion;

    private LocalDateTime fechaDecision;

    private String observacionesAprobacion;

    // =========================================================
    // SOPORTE DOCUMENTAL DE LA APROBACIÓN
    //
    // Puede incorporarse posteriormente.
    // No bloquea la formalización.
    // =========================================================

    private String numeroActa;
    private LocalDate fechaActa;

    // =========================================================
    // CONDICIONES DEFINITIVAS DE FORMALIZACIÓN
    // =========================================================

    private BigDecimal valorFormalizado;
    private Integer plazoFormalizado;

    private String codigoFormaPagoFormalizada;
    private String periodoCodigoInteresFormalizado;
    private String tipoModalidadInteresFormalizado;

    private Integer amortizacionCapitalFormalizada;
    private String codigoTipoCuotaFormalizada;

    private Integer mesesGraciaCapitalFormalizados;
    private Integer mesesGraciaInteresFormalizados;

    private BigDecimal tasaNominalFormalizada;
    private BigDecimal tasaEfectivaAnualFormalizada;
    private BigDecimal valorCuotaFormalizada;

    // =========================================================
    // CONTROL DE FORMALIZACIÓN
    // =========================================================

    private Boolean condicionesModificadas;

    private LocalDateTime fechaFinAprobacion;
    private LocalDateTime fechaFinFormalizacion;
}