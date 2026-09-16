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
public class SolicitudAnalisisComponenteDTO {

    // =========================================================
    // SOLICITUD / DEUDOR
    // =========================================================

    private Integer idSolicitudCredito;
    private String numeroSolicitud;

    private Integer idSolicitudDeudor;
    private Integer idDatosPersonal;

    private String tipoDeudor;
    private Integer ordenDeudor;


    // =========================================================
    // MODELO
    // =========================================================

    private Integer idSolicitudModelo;
    private String versionModelo;
    private String nombreModelo;


    // =========================================================
    // COMPONENTE
    // =========================================================

    private Integer idSolicitudModeloComponente;

    private String codigoComponente;
    private String nombreComponente;

    private Integer ordenComponente;

    private BigDecimal ponderacion;

    private String tipoValor;
    private Boolean obligatorio;


    // =========================================================
    // VALOR EVALUADO
    // =========================================================

    private BigDecimal valorNumericoComponente;
    private String valorTextoComponente;


    // =========================================================
    // REGLA
    // =========================================================

    private Integer idSolicitudModeloRegla;
    private Integer ordenRegla;


    // =========================================================
    // RESULTADO
    // =========================================================

    private BigDecimal puntajeObtenido;
    private BigDecimal puntajePonderado;

    private Boolean cumple;

    private String descripcionResultado;

    private Boolean reglaEncontrada;

    private String estadoComponente;
}