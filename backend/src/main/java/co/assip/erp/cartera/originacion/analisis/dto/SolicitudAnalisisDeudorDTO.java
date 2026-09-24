package co.assip.erp.cartera.originacion.analisis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudAnalisisDeudorDTO {

    // =========================================================
    // SOLICITUD / DEUDOR
    // =========================================================

    private Integer idSolicitudCredito;
    private String numeroSolicitud;

    private Integer idSolicitudDeudor;
    private Integer idDatosPersonal;

    // Identificación del participante
    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    private String tipoDeudor;
    private Integer ordenDeudor;


    // =========================================================
    // MODELO
    // =========================================================

    private Integer idSolicitudModelo;
    private String versionModelo;
    private String nombreModelo;


    // =========================================================
    // ESTADO DEL ANÁLISIS
    // =========================================================

    private Integer cantidadComponentesObligatorios;
    private Integer cantidadComponentesEvaluados;
    private Integer cantidadComponentesPendientes;

    private Boolean analisisCompleto;
    private String componentesPendientes;


    // =========================================================
    // INDICADORES
    // =========================================================

    private BigDecimal indicadorCapacidadPago;
    private BigDecimal indicadorEndeudamiento;
    private BigDecimal indicadorRazonCorriente;

    private BigDecimal puntajeCentralFuente;
    private BigDecimal moraMaxima24Meses;


    // =========================================================
    // PUNTAJES
    // =========================================================

    private BigDecimal puntajeCapacidadPago;
    private BigDecimal puntajeEndeudamiento;
    private BigDecimal puntajeRazonCorriente;

    private BigDecimal puntajeCentralCuantitativo;
    private BigDecimal puntajeCentralCualitativo;

    private BigDecimal puntajeGarantia;
    private BigDecimal puntajeHabitoPagoInterno;

    private BigDecimal puntajeTotal;


    // =========================================================
    // RESULTADO
    // =========================================================

    private Boolean sinCapacidadPago;

    private String perfilRiesgo;
    private String recomendacion;

    private Boolean cumpleOtorgamiento;

    private String motivoResultado;
}