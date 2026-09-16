package co.assip.erp.cartera.originacion.bienes.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class SolicitudBienDTO {

    // =========================================================
    // Solicitud / deudor
    // =========================================================

    private Integer idSolicitudCredito;
    private Integer idSolicitudDeudor;

    private Long idDatosPersonal;

    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    private String tipoDeudor;
    private Integer ordenDeudor;


    // =========================================================
    // Bien actual
    // =========================================================

    private Long idBienPersona;
    private Long idBien;
    private Long idTipoBien;

    private String codigoTipoBien;
    private String nombreTipoBien;

    private String descripcionGeneral;

    private LocalDate fechaAdquisicion;
    private String estadoBien;

    private BigDecimal porcentajePropiedad;


    // =========================================================
    // Valores actuales del bien
    // =========================================================

    private BigDecimal valorComercial;
    private BigDecimal valorGravamen;

    private BigDecimal valorPropiedad;
    private BigDecimal valorGravamenPropiedad;
    private BigDecimal valorNetoPropiedad;


    // =========================================================
    // Créditos que actualmente respalda
    // =========================================================

    private Long cantidadCreditosRespaldados;
    private BigDecimal valorCreditosRespaldados;


    // =========================================================
    // Fotografía / selección en la solicitud
    // =========================================================

    private Integer idSolicitudDeudorBien;

    private Boolean seleccionado;
    private LocalDateTime fechaFotografia;


    // =========================================================
    // Valores congelados para la solicitud
    // =========================================================

    private BigDecimal porcentajeAdmisible;
    private BigDecimal valorGarantiaAdmisible;

    private BigDecimal valorComprometidoCreditos;
    private BigDecimal valorGarantiaDisponible;

    private BigDecimal valorRequeridoSolicitud;
    private BigDecimal valorAsignadoSolicitud;

    private String observacion;
}