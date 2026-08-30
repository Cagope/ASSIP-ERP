package co.assip.erp.cartera.consultacreditos.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta integral de la consulta especializada de créditos.
 *
 * Consolida:
 *
 * - Información general del crédito.
 * - Extracto de pagos.
 * - Configuraciones de seguros.
 * - Alivios.
 * - Intereses causados.
 * - Historial de evaluaciones.
 * - Última evaluación de cartera.
 * - Historial de prórrogas.
 *
 * Este DTO no realiza cálculos.
 * Los valores provienen directamente de las vistas de cartera.
 */
@Getter
@Setter
public class ConsultaCreditoIntegralDTO {

    // =========================================================
    // Crédito seleccionado
    // =========================================================

    private ConsultaCreditoDetalleDTO credito;

    // =========================================================
    // Extracto de pagos
    // =========================================================

    private List<ConsultaCreditoExtractoDTO> extracto;

    // =========================================================
    // Seguros
    // =========================================================

    private List<ConsultaCreditoSeguroDTO> seguros;

    // =========================================================
    // Alivios
    // =========================================================

    private List<ConsultaCreditoAlivioDTO> alivios;

    // =========================================================
    // Intereses causados
    // =========================================================

    private List<ConsultaCreditoInteresDTO> interesesCausados;

    // =========================================================
    // Evaluaciones de cartera
    // =========================================================

    private List<ConsultaCreditoEvaluacionDTO> evaluaciones;

    private ConsultaCreditoEvaluacionDTO ultimaEvaluacion;

    // =========================================================
    // Prórrogas
    // =========================================================

    private List<ConsultaCreditoProrrogaDTO> prorrogas;

    // =========================================================
    // Resultados mensuales de cartera
    // =========================================================

    private List<ConsultaCreditoResultadoMensualDTO> resultadosMensuales;

    // =========================================================
    // Constructor
    // =========================================================

    public ConsultaCreditoIntegralDTO() {

        this.extracto =
                new ArrayList<>();

        this.seguros =
                new ArrayList<>();

        this.alivios =
                new ArrayList<>();

        this.interesesCausados =
                new ArrayList<>();

        this.evaluaciones =
                new ArrayList<>();

        this.prorrogas =
                new ArrayList<>();

        this.resultadosMensuales =
                new ArrayList<>();
    }
}