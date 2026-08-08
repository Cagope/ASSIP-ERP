package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio701;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio701.dto.Criterio701DatoDTO;
import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCriterioResultadoDTO;
import co.assip.erp.cartera.evaluacion.proceso.reglas.EvaluacionReglasService;
import co.assip.erp.cartera.evaluacion.reglas.dto.EvaluacionCriterioReglaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class Criterio701ReestructuracionesService {

    private static final String CODIGO_CRITERIO = "701";

    private static final BigDecimal VALOR_SIN_REESTRUCTURACION =
            BigDecimal.ZERO;

    private static final BigDecimal VALOR_CON_REESTRUCTURACION =
            BigDecimal.ONE;

    private final EvaluacionReglasService evaluacionReglasService;

    // =========================================================
    // EVALUAR CRITERIO 701 - REESTRUCTURACIONES
    // =========================================================

    public EvaluacionCriterioResultadoDTO evaluar(
            Criterio701DatoDTO dato,
            LocalDate fechaCorte
    ) {

        validarSolicitud(
                dato,
                fechaCorte
        );

        BigDecimal valorResultado =
                determinarValorResultado(
                        dato
                );

        /*
         * No se conoce ni se envía ningún código de regla.
         *
         * El selector determina dinámicamente cuál regla
         * corresponde al valor:
         *
         * 0 = sin reestructuración
         * 1 = con reestructuración
         */
        EvaluacionCriterioReglaDTO regla =
                evaluacionReglasService.buscarReglaAplicable(
                        CODIGO_CRITERIO,
                        valorResultado,
                        null
                );

        return construirResultado(
                dato,
                fechaCorte,
                valorResultado,
                regla
        );
    }

    // =========================================================
    // DETERMINAR VALOR RESULTADO
    // =========================================================

    private BigDecimal determinarValorResultado(
            Criterio701DatoDTO dato
    ) {

        return Boolean.TRUE.equals(
                dato.getCreditoReestructurado()
        )
                ? VALOR_CON_REESTRUCTURACION
                : VALOR_SIN_REESTRUCTURACION;
    }

    // =========================================================
    // CONSTRUIR RESULTADO
    // =========================================================

    private EvaluacionCriterioResultadoDTO construirResultado(
            Criterio701DatoDTO dato,
            LocalDate fechaCorte,
            BigDecimal valorResultado,
            EvaluacionCriterioReglaDTO regla
    ) {

        EvaluacionCriterioResultadoDTO resultado =
                new EvaluacionCriterioResultadoDTO();

        resultado.setIdCierreCarteraCredito(
                dato.getIdCierreCarteraCredito()
        );

        resultado.setIdCarteraCredito(
                dato.getIdCarteraCredito()
        );

        resultado.setIdDatosPersonal(
                dato.getIdDatosPersonal()
        );

        resultado.setDocumento(
                dato.getDocumento()
        );

        resultado.setIdEvaluacionCriterio(
                regla.getIdEvaluacionCriterio()
        );

        resultado.setCodigoCriterio(
                regla.getCodigoCriterio()
        );

        resultado.setNombreCriterio(
                regla.getNombreCriterio()
        );

        resultado.setIdEvaluacionCriterioRegla(
                regla.getIdEvaluacionCriterioRegla()
        );

        resultado.setCodigoRegla(
                regla.getCodigoRegla()
        );

        resultado.setNombreRegla(
                regla.getNombreRegla()
        );

        resultado.setValorResultado(
                valorResultado
        );

        resultado.setCodigoResultado(
                regla.getCodigoRegla()
        );

        resultado.setDescripcionResultado(
                regla.getNombreRegla()
        );

        resultado.setPuntajeObtenido(
                regla.getPuntaje() == null
                        ? BigDecimal.ZERO
                        : regla.getPuntaje()
        );

        resultado.setObservaciones(
                construirObservacion(
                        dato,
                        fechaCorte,
                        regla
                )
        );

        return resultado;
    }

    // =========================================================
    // CONSTRUIR OBSERVACIÓN
    // =========================================================

    private String construirObservacion(
            Criterio701DatoDTO dato,
            LocalDate fechaCorte,
            EvaluacionCriterioReglaDTO regla
    ) {

        StringBuilder observacion =
                new StringBuilder();

        observacion
                .append("Fecha de corte: ")
                .append(fechaCorte)
                .append(". Crédito reestructurado: ")
                .append(
                        Boolean.TRUE.equals(
                                dato.getCreditoReestructurado()
                        )
                                ? "SÍ"
                                : "NO"
                );

        if (
                dato.getCodigoModificacionCredito() != null
                        && !dato.getCodigoModificacionCredito().isBlank()
        ) {
            observacion
                    .append(". Código de modificación: ")
                    .append(
                            dato.getCodigoModificacionCredito().trim()
                    );
        }

        if (dato.getFechaReestructuracion() != null) {
            observacion
                    .append(". Fecha de reestructuración: ")
                    .append(
                            dato.getFechaReestructuracion()
                    );
        }

        if (
                dato.getEdadReestructurado() != null
                        && !dato.getEdadReestructurado().isBlank()
        ) {
            observacion
                    .append(". Edad de riesgo reestructurado: ")
                    .append(
                            dato.getEdadReestructurado().trim()
                    );
        }

        observacion
                .append(". Regla aplicada: ")
                .append(
                        regla.getNombreRegla()
                )
                .append(".");

        return observacion.toString();
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarSolicitud(
            Criterio701DatoDTO dato,
            LocalDate fechaCorte
    ) {

        if (dato == null) {
            throw new IllegalArgumentException(
                    "Los datos del crédito a evaluar son obligatorios."
            );
        }

        if (dato.getIdCierreCarteraCredito() == null) {
            throw new IllegalArgumentException(
                    "El identificador del crédito del cierre es obligatorio."
            );
        }

        if (dato.getIdCarteraCredito() == null) {
            throw new IllegalArgumentException(
                    "El identificador del crédito es obligatorio."
            );
        }

        if (dato.getIdDatosPersonal() == null) {
            throw new IllegalArgumentException(
                    "El identificador de la persona es obligatorio."
            );
        }

        if (fechaCorte == null) {
            throw new IllegalArgumentException(
                    "La fecha de corte de la evaluación es obligatoria."
            );
        }

        /*
         * En la fotografía histórica el campo es NOT NULL.
         * Si llega nulo al motor existe una inconsistencia
         * entre la fotografía y el DTO, por lo que no debemos
         * asumir silenciosamente que significa false.
         */
        if (dato.getCreditoReestructurado() == null) {
            throw new IllegalArgumentException(
                    "El crédito "
                            + valorCredito(
                            dato.getIdCarteraCredito()
                    )
                            + " no tiene definido el indicador "
                            + "credito_reestructurado en la fotografía histórica."
            );
        }
    }

    // =========================================================
    // APOYO
    // =========================================================

    private String valorCredito(
            Integer idCarteraCredito
    ) {

        return idCarteraCredito == null
                ? "SIN IDENTIFICADOR"
                : idCarteraCredito.toString();
    }
}