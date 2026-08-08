package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio502;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio502.dto.Criterio502DatoDTO;
import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCriterioResultadoDTO;
import co.assip.erp.cartera.evaluacion.proceso.reglas.EvaluacionReglasService;
import co.assip.erp.cartera.evaluacion.reglas.dto.EvaluacionCriterioReglaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class Criterio502SolvenciaDeudorService {

    private static final String CODIGO_CRITERIO = "502";

    private static final BigDecimal CIEN =
            BigDecimal.valueOf(100);

    /**
     * Valor técnico para ausencia de información patrimonial.
     *
     * La regla y el puntaje se obtienen dinámicamente desde
     * la parametrización del criterio 502.
     */
    private static final BigDecimal VALOR_SIN_INFORMACION =
            BigDecimal.valueOf(-1);

    private final EvaluacionReglasService evaluacionReglasService;

    // =========================================================
    // EVALUAR CRITERIO 502 - SOLVENCIA DEL DEUDOR
    // =========================================================

    public EvaluacionCriterioResultadoDTO evaluar(
            Criterio502DatoDTO dato,
            LocalDate fechaCorte
    ) {

        validarSolicitud(
                dato,
                fechaCorte
        );

        BigDecimal totalActivos =
                valorDecimal(
                        dato.getTotalActivos()
                );

        BigDecimal totalPasivos =
                valorDecimal(
                        dato.getTotalPasivos()
                );

        BigDecimal porcentajePasivos = null;

        BigDecimal valorResultado;

        // =====================================================
        // SIN INFORMACIÓN PATRIMONIAL
        // =====================================================

        if (
                totalActivos.compareTo(
                        BigDecimal.ZERO
                ) == 0
                        &&
                        totalPasivos.compareTo(
                                BigDecimal.ZERO
                        ) == 0
        ) {

            valorResultado =
                    VALOR_SIN_INFORMACION;

        } else {

            // =================================================
            // CALCULAR PORCENTAJE
            // =================================================

            porcentajePasivos =
                    calcularPorcentajePasivos(
                            totalActivos,
                            totalPasivos,
                            dato
                    );

            /*
             * Las reglas actuales del criterio 502 están
             * parametrizadas con rangos enteros:
             *
             * 0 - 20
             * 21 - 40
             * 41 - 60
             * 61 - 70
             * 71 en adelante
             *
             * Por eso el valor técnico se redondea siempre
             * hacia arriba.
             */
            valorResultado =
                    porcentajePasivos.setScale(
                            0,
                            RoundingMode.CEILING
                    );
        }

        EvaluacionCriterioReglaDTO regla =
                evaluacionReglasService.buscarReglaAplicable(
                        CODIGO_CRITERIO,
                        valorResultado,
                        null
                );

        return construirResultado(
                dato,
                fechaCorte,
                totalActivos,
                totalPasivos,
                porcentajePasivos,
                valorResultado,
                regla
        );
    }

    // =========================================================
    // CALCULAR PORCENTAJE DE PASIVOS SOBRE ACTIVOS
    // =========================================================

    private BigDecimal calcularPorcentajePasivos(
            BigDecimal totalActivos,
            BigDecimal totalPasivos,
            Criterio502DatoDTO dato
    ) {

        /*
         * Este caso es diferente de 0/0.
         *
         * Si existen pasivos pero los activos son cero,
         * el porcentaje no puede calcularse matemáticamente.
         *
         * Si aparece este caso se detiene la evaluación para
         * definir su tratamiento funcional explícitamente.
         */
        if (
                totalActivos.compareTo(
                        BigDecimal.ZERO
                ) == 0
        ) {

            /*
             * Si existen pasivos pero no existen activos,
             * la situación patrimonial es desfavorable.
             *
             * Se devuelve un valor técnico de 71 para que
             * el selector aplique dinámicamente la regla
             * "MAYOR A 70%".
             */
            return BigDecimal.valueOf(71);
        }

        return totalPasivos
                .multiply(
                        CIEN
                )
                .divide(
                        totalActivos,
                        4,
                        RoundingMode.HALF_UP
                );
    }

    // =========================================================
    // CONSTRUIR RESULTADO
    // =========================================================

    private EvaluacionCriterioResultadoDTO construirResultado(
            Criterio502DatoDTO dato,
            LocalDate fechaCorte,
            BigDecimal totalActivos,
            BigDecimal totalPasivos,
            BigDecimal porcentajePasivos,
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
                        fechaCorte,
                        totalActivos,
                        totalPasivos,
                        porcentajePasivos,
                        valorResultado,
                        regla
                )
        );

        return resultado;
    }

    // =========================================================
    // CONSTRUIR OBSERVACIÓN
    // =========================================================

    private String construirObservacion(
            LocalDate fechaCorte,
            BigDecimal totalActivos,
            BigDecimal totalPasivos,
            BigDecimal porcentajePasivos,
            BigDecimal valorResultado,
            EvaluacionCriterioReglaDTO regla
    ) {

        if (
                valorResultado.compareTo(
                        VALOR_SIN_INFORMACION
                ) == 0
        ) {

            return "Fecha de corte: "
                    + fechaCorte
                    + ". Activos totales históricos: 0"
                    + ". Pasivos totales históricos: 0"
                    + ". La fotografía histórica no contiene "
                    + "información patrimonial utilizable para calcular "
                    + "la solvencia del deudor. Regla aplicada: "
                    + regla.getNombreRegla()
                    + ".";
        }

        return "Fecha de corte: "
                + fechaCorte
                + ". Activos totales históricos: "
                + totalActivos
                .stripTrailingZeros()
                .toPlainString()
                + ". Pasivos totales históricos: "
                + totalPasivos
                .stripTrailingZeros()
                .toPlainString()
                + ". Porcentaje de pasivos sobre activos: "
                + porcentajePasivos
                .stripTrailingZeros()
                .toPlainString()
                + "%. Valor utilizado para seleccionar la regla: "
                + valorResultado
                .stripTrailingZeros()
                .toPlainString()
                + "%. Regla aplicada: "
                + regla.getNombreRegla()
                + ".";
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarSolicitud(
            Criterio502DatoDTO dato,
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

        validarValorNoNegativo(
                dato.getTotalActivos(),
                "activos totales",
                dato
        );

        validarValorNoNegativo(
                dato.getTotalPasivos(),
                "pasivos totales",
                dato
        );
    }

    private void validarValorNoNegativo(
            BigDecimal valor,
            String nombreCampo,
            Criterio502DatoDTO dato
    ) {

        if (
                valor != null
                        && valor.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new IllegalArgumentException(
                    "Los "
                            + nombreCampo
                            + " de la persona "
                            + valorDocumento(
                            dato.getDocumento()
                    )
                            + ", crédito "
                            + valorCredito(
                            dato.getIdCarteraCredito()
                    )
                            + ", no pueden ser negativos."
            );
        }
    }

    // =========================================================
    // APOYO
    // =========================================================

    private BigDecimal valorDecimal(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }

    private String valorCredito(
            Integer idCarteraCredito
    ) {

        return idCarteraCredito == null
                ? "SIN IDENTIFICADOR"
                : idCarteraCredito.toString();
    }

    private String valorDocumento(
            String documento
    ) {

        if (
                documento == null
                        || documento.isBlank()
        ) {
            return "SIN DOCUMENTO";
        }

        return documento.trim();
    }
}