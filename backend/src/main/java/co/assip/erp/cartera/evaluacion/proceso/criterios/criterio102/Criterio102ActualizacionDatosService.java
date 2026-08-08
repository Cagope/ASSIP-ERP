package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio102;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio102.dto.Criterio102DatoDTO;
import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCriterioResultadoDTO;
import co.assip.erp.cartera.evaluacion.proceso.reglas.EvaluacionReglasService;
import co.assip.erp.cartera.evaluacion.reglas.dto.EvaluacionCriterioReglaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class Criterio102ActualizacionDatosService {

    /*
     * El código del criterio permanece fijo porque esta clase
     * implementa específicamente el criterio 102.
     *
     * Los códigos, rangos y puntajes de las reglas se obtienen
     * dinámicamente desde la base de datos.
     */
    private static final String CODIGO_CRITERIO = "102";

    private final EvaluacionReglasService evaluacionReglasService;

    // =========================================================
    // EVALUAR CRITERIO 102 - ACTUALIZACIÓN DE DATOS
    // =========================================================

    public EvaluacionCriterioResultadoDTO evaluar(
            Criterio102DatoDTO dato,
            LocalDate fechaCorte,
            BigDecimal diasMaximoActualizacion
    ) {

        validarSolicitud(
                dato,
                fechaCorte,
                diasMaximoActualizacion
        );

        LocalDate fechaActualizacion =
                dato.getFechaActualizacionDatos();

        long diasTranscurridos =
                calcularDiasTranscurridos(
                        fechaActualizacion,
                        fechaCorte,
                        diasMaximoActualizacion
                );

        BigDecimal valorResultado =
                BigDecimal.valueOf(
                        diasTranscurridos
                );

        /*
         * No se envía ningún código de regla.
         *
         * El selector revisa todas las reglas activas del criterio
         * y determina cuál aplica al valor calculado.
         *
         * El criterio 102 no diferencia por tipo de persona,
         * por eso idTipoPersona se envía en null.
         */
        EvaluacionCriterioReglaDTO regla =
                evaluacionReglasService.buscarReglaAplicable(
                        CODIGO_CRITERIO,
                        valorResultado,
                        null
                );

        return construirResultado(
                dato,
                fechaActualizacion,
                fechaCorte,
                diasTranscurridos,
                diasMaximoActualizacion,
                valorResultado,
                regla
        );
    }

    // =========================================================
    // CALCULAR DÍAS TRANSCURRIDOS
    // =========================================================

    private long calcularDiasTranscurridos(
            LocalDate fechaActualizacion,
            LocalDate fechaCorte,
            BigDecimal diasMaximoActualizacion
    ) {

        /*
         * Cuando no existe fecha de actualización, se asigna un
         * valor inmediatamente superior al máximo permitido.
         *
         * Así se selecciona dinámicamente la regla configurada
         * para información desactualizada.
         */
        if (fechaActualizacion == null) {
            return diasMaximoActualizacion
                    .longValue()
                    + 1L;
        }

        long diasTranscurridos =
                ChronoUnit.DAYS.between(
                        fechaActualizacion,
                        fechaCorte
                );

        /*
         * En fotografías históricas reconstruidas puede aparecer
         * una fecha de actualización posterior al corte.
         *
         * Mientras se corrige el origen histórico, se considera
         * vigente para ese corte y se asignan cero días.
         */
        return Math.max(
                diasTranscurridos,
                0L
        );
    }

    // =========================================================
    // CONSTRUIR RESULTADO
    // =========================================================

    private EvaluacionCriterioResultadoDTO construirResultado(
            Criterio102DatoDTO dato,
            LocalDate fechaActualizacion,
            LocalDate fechaCorte,
            long diasTranscurridos,
            BigDecimal diasMaximoActualizacion,
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
         * El código y la descripción del resultado provienen
         * de la regla seleccionada dinámicamente.
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
                        fechaActualizacion,
                        fechaCorte,
                        diasTranscurridos,
                        diasMaximoActualizacion,
                        regla
                )
        );

        return resultado;
    }

    // =========================================================
    // CONSTRUIR OBSERVACIÓN
    // =========================================================

    private String construirObservacion(
            LocalDate fechaActualizacion,
            LocalDate fechaCorte,
            long diasTranscurridos,
            BigDecimal diasMaximoActualizacion,
            EvaluacionCriterioReglaDTO regla
    ) {

        if (fechaActualizacion == null) {
            return "El asociado no tiene registrada una fecha "
                    + "de actualización de datos. "
                    + "Valor técnico utilizado para seleccionar la regla: "
                    + diasTranscurridos
                    + ". Regla aplicada: "
                    + regla.getNombreRegla()
                    + ".";
        }

        return "Fecha de actualización: "
                + fechaActualizacion
                + ". Fecha de corte: "
                + fechaCorte
                + ". Días transcurridos al corte: "
                + diasTranscurridos
                + ". Máximo permitido: "
                + diasMaximoActualizacion
                .stripTrailingZeros()
                .toPlainString()
                + ". Regla aplicada: "
                + regla.getNombreRegla()
                + ".";
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarSolicitud(
            Criterio102DatoDTO dato,
            LocalDate fechaCorte,
            BigDecimal diasMaximoActualizacion
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

        if (
                diasMaximoActualizacion == null
                        || diasMaximoActualizacion.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new IllegalArgumentException(
                    "El parámetro 121 — Días máximo de actualización "
                            + "no está configurado correctamente."
            );
        }
    }
}