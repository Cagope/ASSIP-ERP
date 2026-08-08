package co.assip.erp.cartera.evaluacion.proceso.resultados.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EvaluacionResultadoCreditoDTO {

    // =========================================================
    // IDENTIFICACIÓN DEL RESULTADO
    // =========================================================

    private Integer idEvaluacionCarteraCredito;

    private Integer idEvaluacionCartera;

    private Integer idCierreCarteraCredito;

    private Integer idCarteraCredito;


    // =========================================================
    // ASOCIADO
    // =========================================================

    private Integer idDatosPersonal;

    private String documento;

    private String nombres;

    private String primerApellido;

    private String segundoApellido;


    // =========================================================
    // AGENCIA
    // =========================================================

    private Integer idAgencia;

    private String codigoAgencia;

    private String nombreAgencia;


    // =========================================================
    // LÍNEA DE CRÉDITO
    // =========================================================

    private Integer idLineaCredito;

    private String codigoLineaCredito;

    private String nombreLineaCredito;


    // =========================================================
    // CRÉDITO
    // =========================================================

    private String pagareCartera;

    private String codigoClasificacionCredito;

    private BigDecimal valorInicialCredito;

    private BigDecimal saldoActual;


    // =========================================================
    // RESULTADO DE LA EVALUACIÓN
    // =========================================================

    private BigDecimal puntajeTotal;

    private String edadMora;

    private String edadRiesgoAnterior;

    private String edadRiesgoInicial;

    private String edadRiesgoCalculada;

    private String edadRiesgoArrastre;

    private String edadRiesgoFinal;


    // =========================================================
    // RECOMENDACIÓN
    //
    // R = Reclasificar
    // H = Habilitar
    // M = Mantener
    // =========================================================

    private String accionEvaluacion;

    private String comentarioEvaluacion;


    // =========================================================
    // APOYO PARA PRESENTACIÓN
    // =========================================================

    public String getNombreCompleto() {

        StringBuilder nombre =
                new StringBuilder();

        agregarParte(
                nombre,
                nombres
        );

        agregarParte(
                nombre,
                primerApellido
        );

        agregarParte(
                nombre,
                segundoApellido
        );

        return nombre.toString();
    }


    public String getDescripcionAccion() {

        if (accionEvaluacion == null) {
            return "";
        }

        return switch (
                accionEvaluacion
                        .trim()
                        .toUpperCase()
                ) {

            case "R" ->
                    "Reclasificar";

            case "H" ->
                    "Habilitar";

            case "M" ->
                    "Mantener";

            default ->
                    accionEvaluacion;
        };
    }


    // =========================================================
    // APOYO INTERNO
    // =========================================================

    private void agregarParte(
            StringBuilder texto,
            String valor
    ) {

        if (
                valor == null
                        || valor.isBlank()
        ) {
            return;
        }

        if (!texto.isEmpty()) {
            texto.append(" ");
        }

        texto.append(
                valor.trim()
        );
    }
}