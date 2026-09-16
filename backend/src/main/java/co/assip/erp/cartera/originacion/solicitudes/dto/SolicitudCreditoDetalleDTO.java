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
public class SolicitudCreditoDetalleDTO {

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
    // ASOCIADO PRINCIPAL
    // =========================================================

    private Integer idDatosPersonal;

    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;


    // =========================================================
    // CUENTA DE APORTES
    // =========================================================

    private Integer idCuentaAportes;


    // =========================================================
    // DATOS DEL CRÉDITO
    // =========================================================

    private Integer idLineaCredito;
    private String nombreLineaCredito;

    private String codigoClasificacionCredito;
    private String nombreClasificacionCredito;

    private String codigoDestinoEconomico;
    private String nombreDestinoEconomico;


    // =========================================================
    // GARANTÍA
    // =========================================================

    private String codigoGarantiaCredito;
    private String nombreGarantiaCredito;

    private String codigoSubgarantia;
    private String nombreSubgarantia;

    // =========================================================
    // FONDO DE GARANTÍAS
    // =========================================================

    private Integer idFondoGarantia;
    private String codigoFondoGarantia;
    private String nombreFondoGarantia;

    private BigDecimal porcentajeFondoAplicado;
    private String formaCobroFondo;
    private BigDecimal valorFondoGarantia;

    // =========================================================
    // FORMA DE PAGO
    // =========================================================

    private String codigoFormaPago;
    private String nombreFormaPago;


    // =========================================================
    // MODALIDAD DE INTERÉS
    // =========================================================

    private String periodoCodigoInteres;
    private String tipoModalidadInteres;
    private String nombreModalidadInteres;

    private Integer amortizacionCapital;


    // =========================================================
    // TIPO DE CUOTA
    // =========================================================

    private String codigoTipoCuota;
    private String nombreTipoCuota;


    // =========================================================
    // PLAZO Y VALOR SOLICITADO
    // =========================================================

    private Integer plazoSolicitado;

    private Integer mesesGraciaCapital;
    private Integer mesesGraciaInteres;

    private BigDecimal valorSolicitado;


    // =========================================================
    // LIBRANZA
    // =========================================================

    private Integer idEmpresaLibranza;
    private String nombreEmpresaLibranza;


    // =========================================================
    // CONDICIÓN INICIAL APLICADA
    // =========================================================

    private Integer idCondicionInicial;
    private Integer idCondicionInicialDetalle;

    private Integer plazoMinimoAplicado;
    private Integer plazoMaximoAplicado;

    private BigDecimal cantidadSmmlvMinimoAplicada;
    private BigDecimal cantidadSmmlvMaximoAplicada;

    private BigDecimal factorReciprocidadAportesAplicado;

    private BigDecimal valorSmmlvAplicado;
    private BigDecimal cantidadSmmlvSolicitada;


    // =========================================================
    // APORTES
    // =========================================================

    private BigDecimal valorAportesInicio;
    private Boolean cumpleAportesInicio;

    private BigDecimal valorAportesValidacion;
    private Boolean cumpleAportesValidacion;

    private BigDecimal cupoMaximoPorAportes;
    private BigDecimal valorAportesRequerido;


    // =========================================================
    // TASA Y CUOTA PROYECTADA
    // =========================================================

    private Integer idTasaColocacionDetalle;

    private BigDecimal tasaColocacionAplicada;
    private BigDecimal tasaEfectivaAnual;

    private BigDecimal valorCuotaProyectada;

    // =========================================================
    // ENTE APROBADOR
    // =========================================================

    private Integer idEnteAprobacion;
    private String nombreEnteAprobacion;

    private String motivoAprobacion;

    private Boolean esDirectivo;
    private Boolean esPrivilegiado;


    // =========================================================
    // DIRECTIVO
    // =========================================================

    private String nombreTipoDirectivoAsociado;
    private String nombreCalidadDirectivoAsociado;


    // =========================================================
    // RELACIÓN CON DIRECTIVO
    // =========================================================

    private String nombreParentesco;

    private Integer idDatosPersonalDirectivo;
    private String documentoDirectivo;
    private String nombreDirectivo;

    private String nombreTipoDirectivoRelacionado;
    private String nombreCalidadDirectivoRelacionado;

    // =========================================================
    // TOPE DE APROBACIÓN
    // =========================================================

    private BigDecimal valorTopeSmmlv;
    private BigDecimal valorTopePesos;


    // =========================================================
    // MENSAJES DE APROBACIÓN
    // =========================================================

    private String mensajeAprobacion;
    private String detalleAprobacion;


    // =========================================================
    // ESTADO DE LA SOLICITUD
    // =========================================================

    private Integer idSolicitudProceso;
    private String nombreProceso;

    private Integer idSolicitudResultado;
    private String nombreResultado;
    private Boolean resultadoFinal;


    // =========================================================
    // CRÉDITO GENERADO
    // =========================================================

    private Integer idCarteraCredito;


    // =========================================================
    // OBSERVACIONES
    // =========================================================

    private String observacionAsesor;


    // =========================================================
    // FECHAS DEL FLUJO
    // =========================================================

    private LocalDateTime fechaFinIniciada;
    private LocalDateTime fechaFinDocumentacion;
    private LocalDateTime fechaFinAprobacion;
    private LocalDateTime fechaFinDesembolso;


    // =========================================================
    // CONTROL
    // =========================================================

    private Boolean activo;
}