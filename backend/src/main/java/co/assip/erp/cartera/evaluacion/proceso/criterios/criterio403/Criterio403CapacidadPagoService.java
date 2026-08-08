package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio403;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio403.dto.Criterio403DatoDTO;
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
public class Criterio403CapacidadPagoService {

    // =========================================================
    // CONSTANTES
    // =========================================================

    private static final String CODIGO_CRITERIO = "403";

    private static final BigDecimal CIEN =
            BigDecimal.valueOf(100);

    /**
     * Valor técnico utilizado cuando la fotografía histórica
     * no contiene información económica utilizable.
     *
     * La regla concreta y su puntaje se obtienen dinámicamente
     * desde la parametrización del criterio.
     */
    private static final BigDecimal VALOR_SIN_INFORMACION =
            BigDecimal.valueOf(-1);

    private final EvaluacionReglasService evaluacionReglasService;

    // =========================================================
    // EVALUAR CRITERIO 403 - CAPACIDAD DE PAGO
    // =========================================================

    public EvaluacionCriterioResultadoDTO evaluar(
            Criterio403DatoDTO dato,
            LocalDate fechaCorte
    ) {

        validarSolicitud(
                dato,
                fechaCorte
        );

        BigDecimal ingresosTotales =
                valorDecimal(
                        dato.getIngresosTotales()
                );

        BigDecimal egresosTotales =
                valorDecimal(
                        dato.getEgresosTotales()
                );

        BigDecimal porcentajeEgresos = null;

        BigDecimal valorResultado;

        // =====================================================
        // SIN INFORMACIÓN ECONÓMICA
        // =====================================================

        if (
                ingresosTotales.compareTo(
                        BigDecimal.ZERO
                ) == 0
                        &&
                        egresosTotales.compareTo(
                                BigDecimal.ZERO
                        ) == 0
        ) {

            valorResultado =
                    VALOR_SIN_INFORMACION;

        } else {

            // =================================================
            // CALCULAR PORCENTAJE
            // =================================================

            porcentajeEgresos =
                    calcularPorcentajeEgresos(
                            ingresosTotales,
                            egresosTotales,
                            dato
                    );

            /*
             * Las reglas actuales del criterio 403 están
             * parametrizadas con límites enteros:
             *
             * 0 - 20
             * 21 - 30
             * 31 - 35
             * 36 - 40
             * 41 - 50
             * 51 - 60
             * 61 - 80
             * 81 en adelante
             *
             * Por eso el valor técnico se redondea siempre
             * hacia arriba.
             *
             * Ejemplo:
             * 20.01 % -> 21
             */
            valorResultado =
                    porcentajeEgresos.setScale(
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
                ingresosTotales,
                egresosTotales,
                porcentajeEgresos,
                valorResultado,
                regla
        );
    }

    // =========================================================
    // CALCULAR PORCENTAJE DE EGRESOS SOBRE INGRESOS
    // =========================================================

    private BigDecimal calcularPorcentajeEgresos(
            BigDecimal ingresosTotales,
            BigDecimal egresosTotales,
            Criterio403DatoDTO dato
    ) {

        /*
         * Este caso no debe confundirse con 0/0.
         *
         * Si existen egresos pero no existen ingresos,
         * matemáticamente el porcentaje no puede calcularse.
         *
         * Si llegara a aparecer en los datos, se detiene la
         * evaluación para definir explícitamente su tratamiento.
         */
        if (
                ingresosTotales.compareTo(
                        BigDecimal.ZERO
                ) == 0
        ) {
            throw new IllegalArgumentException(
                    "El crédito "
                            + valorCredito(
                            dato.getIdCarteraCredito()
                    )
                            + " pertenece a la persona "
                            + valorDocumento(
                            dato.getDocumento()
                    )
                            + " cuya fotografía histórica registra "
                            + "ingresos totales iguales a cero y "
                            + "egresos mayores a cero. "
                            + "Este caso requiere tratamiento funcional "
                            + "específico para el criterio 403."
            );
        }

        return egresosTotales
                .multiply(
                        CIEN
                )
                .divide(
                        ingresosTotales,
                        4,
                        RoundingMode.HALF_UP
                );
    }

    // =========================================================
    // CONSTRUIR RESULTADO
    // =========================================================

    private EvaluacionCriterioResultadoDTO construirResultado(
            Criterio403DatoDTO dato,
            LocalDate fechaCorte,
            BigDecimal ingresosTotales,
            BigDecimal egresosTotales,
            BigDecimal porcentajeEgresos,
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
                        ingresosTotales,
                        egresosTotales,
                        porcentajeEgresos,
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
            BigDecimal ingresosTotales,
            BigDecimal egresosTotales,
            BigDecimal porcentajeEgresos,
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
                    + ". Ingresos totales históricos: 0"
                    + ". Egresos totales históricos: 0"
                    + ". La fotografía histórica no contiene "
                    + "información económica utilizable para calcular "
                    + "la capacidad de pago. Regla aplicada: "
                    + regla.getNombreRegla()
                    + ".";
        }

        return "Fecha de corte: "
                + fechaCorte
                + ". Ingresos totales históricos: "
                + ingresosTotales
                .stripTrailingZeros()
                .toPlainString()
                + ". Egresos totales históricos: "
                + egresosTotales
                .stripTrailingZeros()
                .toPlainString()
                + ". Porcentaje de egresos sobre ingresos: "
                + porcentajeEgresos
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
            Criterio403DatoDTO dato,
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
                dato.getIngresosTotales(),
                "ingresos totales",
                dato
        );

        validarValorNoNegativo(
                dato.getEgresosTotales(),
                "egresos totales",
                dato
        );
    }

    private void validarValorNoNegativo(
            BigDecimal valor,
            String nombreCampo,
            Criterio403DatoDTO dato
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