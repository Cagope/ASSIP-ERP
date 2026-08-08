package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio201;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio201.dto.Criterio201DatoDTO;
import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCriterioResultadoDTO;
import co.assip.erp.cartera.evaluacion.proceso.reglas.EvaluacionReglasService;
import co.assip.erp.cartera.evaluacion.reglas.dto.EvaluacionCriterioReglaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class Criterio201EdadCronologicaService {

    /*
     * Este código permanece fijo porque esta clase implementa
     * específicamente el criterio 201.
     *
     * Los códigos de reglas, rangos y puntajes se obtienen
     * dinámicamente desde la base de datos.
     */
    private static final String CODIGO_CRITERIO = "201";

    private static final short TIPO_PERSONA_NATURAL = 1;

    private static final short TIPO_PERSONA_JURIDICA = 2;

    private final EvaluacionReglasService evaluacionReglasService;

    // =========================================================
    // EVALUAR CRITERIO 201 - EDAD CRONOLÓGICA
    // =========================================================

    public EvaluacionCriterioResultadoDTO evaluar(
            Criterio201DatoDTO dato,
            LocalDate fechaCorte
    ) {

        validarSolicitud(
                dato,
                fechaCorte
        );

        Short idTipoPersona =
                dato.getIdTipoPersona();

        LocalDate fechaNacimiento =
                dato.getFechaNacimiento();

        int aniosCompletos =
                calcularAniosCompletos(
                        fechaNacimiento,
                        fechaCorte
                );

        BigDecimal valorResultado =
                BigDecimal.valueOf(
                        aniosCompletos
                );

        /*
         * El selector consulta todas las reglas activas del
         * criterio 201 correspondientes al tipo de persona.
         *
         * No se conoce ni se envía ningún código de regla.
         */
        EvaluacionCriterioReglaDTO regla =
                evaluacionReglasService.buscarReglaAplicable(
                        CODIGO_CRITERIO,
                        valorResultado,
                        idTipoPersona
                );

        return construirResultado(
                dato,
                fechaCorte,
                fechaNacimiento,
                aniosCompletos,
                valorResultado,
                regla
        );
    }

    // =========================================================
    // CALCULAR AÑOS COMPLETOS
    // =========================================================

    private int calcularAniosCompletos(
            LocalDate fechaNacimiento,
            LocalDate fechaCorte
    ) {

        if (fechaNacimiento.isAfter(fechaCorte)) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento "
                            + fechaNacimiento
                            + " no puede ser posterior a la fecha de corte "
                            + fechaCorte
                            + "."
            );
        }

        return Period.between(
                fechaNacimiento,
                fechaCorte
        ).getYears();
    }

    // =========================================================
    // CONSTRUIR RESULTADO
    // =========================================================

    private EvaluacionCriterioResultadoDTO construirResultado(
            Criterio201DatoDTO dato,
            LocalDate fechaCorte,
            LocalDate fechaNacimiento,
            int aniosCompletos,
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
         * El código del resultado proviene de la regla
         * encontrada dinámicamente.
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
                        dato.getIdTipoPersona(),
                        fechaNacimiento,
                        fechaCorte,
                        aniosCompletos,
                        regla
                )
        );

        return resultado;
    }

    // =========================================================
    // CONSTRUIR OBSERVACIÓN
    // =========================================================

    private String construirObservacion(
            Short idTipoPersona,
            LocalDate fechaNacimiento,
            LocalDate fechaCorte,
            int aniosCompletos,
            EvaluacionCriterioReglaDTO regla
    ) {

        String concepto =
                idTipoPersona == TIPO_PERSONA_NATURAL
                        ? "Edad cronológica"
                        : "Antigüedad de la persona jurídica";

        return concepto
                + ": "
                + aniosCompletos
                + " años completos. Fecha base: "
                + fechaNacimiento
                + ". Fecha de corte: "
                + fechaCorte
                + ". Regla aplicada: "
                + regla.getNombreRegla()
                + ".";
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarSolicitud(
            Criterio201DatoDTO dato,
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

        validarTipoPersona(
                dato.getIdTipoPersona()
        );

        if (dato.getFechaNacimiento() == null) {
            throw new IllegalArgumentException(
                    "La persona con documento "
                            + valorDocumento(
                            dato.getDocumento()
                    )
                            + " no tiene registrada la fecha de nacimiento "
                            + "requerida por el criterio 201."
            );
        }
    }

    private void validarTipoPersona(
            Short idTipoPersona
    ) {

        if (idTipoPersona == null) {
            throw new IllegalArgumentException(
                    "El tipo de persona es obligatorio para evaluar "
                            + "el criterio 201."
            );
        }

        if (
                idTipoPersona != TIPO_PERSONA_NATURAL
                        && idTipoPersona != TIPO_PERSONA_JURIDICA
        ) {
            throw new IllegalArgumentException(
                    "El tipo de persona "
                            + idTipoPersona
                            + " no es válido para evaluar el criterio 201."
            );
        }
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