package co.assip.erp.cartera.analisis.moratemprana.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MoraTempranaDetalleDTO {

    // =========================================================
    // Crédito
    // =========================================================

    private Long idCarteraCredito;

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    private Integer idLineaCredito;
    private String codigoLineaCredito;
    private String nombreLineaCredito;

    private String pagareCartera;

    // =========================================================
    // Asociado
    // =========================================================

    private Long idDatosPersonal;

    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    // =========================================================
    // Originación
    // =========================================================

    private LocalDate cosecha;
    private LocalDate fechaDesembolso;

    private BigDecimal valorInicialCredito;
    private BigDecimal valorDesembolsado;

    // =========================================================
    // Mora temprana
    // =========================================================

    private Integer primerMob30;
    private Integer primerMob60;

    private Integer maxDiasMoraHastaMob6;

    private Boolean mora30HastaMob3;
    private Boolean mora30HastaMob6;
    private Boolean mora60HastaMob6;

    // =========================================================
    // Situación observada
    // =========================================================

    private LocalDate fechaUltimoCorteObservado;
    private Integer mobUltimoCorteObservado;

    private BigDecimal saldoUltimoCorteObservado;
    private Integer diasMoraUltimoCorteObservado;

    // =========================================================
    // Contacto
    // =========================================================

    private String telefono;
    private String celular;
    private String correo;
}