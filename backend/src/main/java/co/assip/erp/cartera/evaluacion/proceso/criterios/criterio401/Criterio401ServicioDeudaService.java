package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio401;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio401.dto.Criterio401DatoDTO;
import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCriterioResultadoDTO;
import co.assip.erp.cartera.evaluacion.proceso.reglas.EvaluacionReglasService;
import co.assip.erp.cartera.evaluacion.reglas.dto.EvaluacionCriterioReglaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class Criterio401ServicioDeudaService {

    /*
     * Este código permanece fijo porque esta clase implementa
     * específicamente el criterio 401.
     *
     * Los códigos de las reglas, rangos y puntajes se obtienen
     * dinámicamente desde la base de datos.
     */
    private static final String CODIGO_CRITERIO = "401";

    /*
     * Valor técnico utilizado cuando el crédito no registra
     * pagos evaluables durante el periodo analizado.
     *
     * No identifica una regla concreta. El selector genérico
     * buscará la regla activa configurada para este valor.
     */
    private static final BigDecimal VALOR_SIN_INFORMACION =
            BigDecimal.valueOf(-1);

    private final EvaluacionReglasService evaluacionReglasService;

    // =========================================================
    // EVALUAR CRITERIO 401 - SERVICIO DE LA DEUDA
    // =========================================================

    public EvaluacionCriterioResultadoDTO evaluar(
            Criterio401DatoDTO dato,
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
            Criterio401DatoDTO dato
    ) {

        int cantidadPagosEvaluables =
                valorEntero(
                        dato.getCantidadPagosEvaluablesUltimoAnio()
                );

        /*
         * Cada pago ya llega consolidado por:
         *
         * id_cartera_credito
         * + tipo_comprobante
         * + numero_comprobante
         *
         * Si no existe ningún comprobante consolidado con
         * dias_mora informado, el crédito se considera
         * sin información evaluable.
         */
        if (cantidadPagosEvaluables <= 0) {
            return VALOR_SIN_INFORMACION;
        }

        BigDecimal promedioDiasMora =
                dato.getPromedioDiasMoraUltimoAnio();

        if (promedioDiasMora == null) {
            throw new IllegalArgumentException(
                    "El crédito "
                            + valorCredito(
                            dato.getIdCarteraCredito()
                    )
                            + " registra pagos evaluables, pero no tiene "
                            + "promedio de días de mora calculado."
            );
        }

        if (
                promedioDiasMora.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new IllegalArgumentException(
                    "El promedio de días de mora del crédito "
                            + valorCredito(
                            dato.getIdCarteraCredito()
                    )
                            + " no puede ser negativo."
            );
        }

        return promedioDiasMora;
    }

    // =========================================================
    // CONSTRUIR RESULTADO
    // =========================================================

    private EvaluacionCriterioResultadoDTO construirResultado(
            Criterio401DatoDTO dato,
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

        /*
         * El código y la descripción provienen de la regla
         * seleccionada dinámicamente.
         */
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
            Criterio401DatoDTO dato,
            LocalDate fechaCorte,
            BigDecimal valorResultado,
            EvaluacionCriterioReglaDTO regla
    ) {

        LocalDate fechaInicial =
                fechaCorte
                        .minusYears(1)
                        .plusDays(1);

        int cantidadPagos =
                valorEntero(
                        dato.getCantidadPagosUltimoAnio()
                );

        int cantidadPagosCapital =
                valorEntero(
                        dato.getCantidadPagosCapitalUltimoAnio()
                );

        int cantidadPagosEvaluables =
                valorEntero(
                        dato.getCantidadPagosEvaluablesUltimoAnio()
                );

        BigDecimal sumaDiasMora =
                valorDecimal(
                        dato.getSumaDiasMoraUltimoAnio()
                );

        if (
                valorResultado.compareTo(
                        VALOR_SIN_INFORMACION
                ) == 0
        ) {

            return "Periodo analizado: "
                    + fechaInicial
                    + " a "
                    + fechaCorte
                    + ". Pagos consolidados: "
                    + cantidadPagos
                    + ". Pagos con abono a capital: "
                    + cantidadPagosCapital
                    + ". Pagos evaluables: "
                    + cantidadPagosEvaluables
                    + ". El crédito no registra pagos con días de mora "
                    + "informados durante el periodo. "
                    + "Regla aplicada: "
                    + regla.getNombreRegla()
                    + ".";
        }

        return "Periodo analizado: "
                + fechaInicial
                + " a "
                + fechaCorte
                + ". Pagos consolidados: "
                + cantidadPagos
                + ". Pagos con abono a capital: "
                + cantidadPagosCapital
                + ". Pagos evaluables: "
                + cantidadPagosEvaluables
                + ". Suma de días de mora: "
                + sumaDiasMora
                .stripTrailingZeros()
                .toPlainString()
                + ". Promedio de mora redondeado hacia arriba: "
                + valorResultado
                .stripTrailingZeros()
                .toPlainString()
                + " días. Regla aplicada: "
                + regla.getNombreRegla()
                + ".";
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarSolicitud(
            Criterio401DatoDTO dato,
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

        validarCantidadNoNegativa(
                dato.getCantidadPagosUltimoAnio(),
                "cantidad de pagos consolidados del último año",
                dato.getIdCarteraCredito()
        );

        validarCantidadNoNegativa(
                dato.getCantidadPagosCapitalUltimoAnio(),
                "cantidad de pagos con capital del último año",
                dato.getIdCarteraCredito()
        );

        validarCantidadNoNegativa(
                dato.getCantidadPagosEvaluablesUltimoAnio(),
                "cantidad de pagos evaluables del último año",
                dato.getIdCarteraCredito()
        );

        validarCoherenciaCantidades(
                dato
        );
    }

    private void validarCantidadNoNegativa(
            Integer cantidad,
            String nombreCampo,
            Integer idCarteraCredito
    ) {

        if (
                cantidad != null
                        && cantidad < 0
        ) {
            throw new IllegalArgumentException(
                    "La "
                            + nombreCampo
                            + " del crédito "
                            + valorCredito(
                            idCarteraCredito
                    )
                            + " no puede ser negativa."
            );
        }
    }

    private void validarCoherenciaCantidades(
            Criterio401DatoDTO dato
    ) {

        int cantidadPagos =
                valorEntero(
                        dato.getCantidadPagosUltimoAnio()
                );

        int cantidadPagosCapital =
                valorEntero(
                        dato.getCantidadPagosCapitalUltimoAnio()
                );

        int cantidadPagosEvaluables =
                valorEntero(
                        dato.getCantidadPagosEvaluablesUltimoAnio()
                );

        if (
                cantidadPagosCapital
                        > cantidadPagos
        ) {
            throw new IllegalArgumentException(
                    "La cantidad de pagos con capital del crédito "
                            + valorCredito(
                            dato.getIdCarteraCredito()
                    )
                            + " no puede superar la cantidad total "
                            + "de pagos consolidados."
            );
        }

        if (
                cantidadPagosEvaluables
                        > cantidadPagos
        ) {
            throw new IllegalArgumentException(
                    "La cantidad de pagos evaluables del crédito "
                            + valorCredito(
                            dato.getIdCarteraCredito()
                    )
                            + " no puede superar la cantidad total "
                            + "de pagos consolidados."
            );
        }
    }

    // =========================================================
    // APOYO
    // =========================================================

    private int valorEntero(
            Integer valor
    ) {

        return valor == null
                ? 0
                : valor;
    }

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
}