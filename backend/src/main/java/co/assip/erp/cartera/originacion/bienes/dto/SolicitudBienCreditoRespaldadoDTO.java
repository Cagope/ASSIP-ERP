package co.assip.erp.cartera.originacion.bienes.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SolicitudBienCreditoRespaldadoDTO {

    // =========================================================
    // Bien
    // =========================================================

    private Long idBien;


    // =========================================================
    // Crédito respaldado
    // =========================================================

    private Long idCarteraCredito;
    private Long idObligacionJuridica;

    private Integer idAgencia;
    private Integer idLineaCredito;

    private String pagareCartera;


    // =========================================================
    // Titular del crédito
    // =========================================================

    private Long idDatosPersonal;

    private String documento;
    private String nombreCompleto;


    // =========================================================
    // Información económica del crédito
    // =========================================================

    private LocalDate fechaDesembolso;

    private BigDecimal valorInicialCredito;
    private BigDecimal valorDesembolsado;
    private BigDecimal valorCuota;
    private BigDecimal saldoActual;


    // =========================================================
    // Garantía del crédito
    // =========================================================

    private String codigoGarantiaCredito;
    private String descripcionGarantiaCredito;
    private String tipoGarantia;


    // =========================================================
    // Estado actual
    // =========================================================

    private String codigoEstadoCartera;
    private String codigoEstadoJuridico;
}