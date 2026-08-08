package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio402;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio402.dto.Criterio402DatoDTO;
import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCreditoDTO;
import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCriterioResultadoDTO;
import co.assip.erp.cartera.evaluacion.proceso.reglas.EvaluacionReglasService;
import co.assip.erp.cartera.evaluacion.reglas.dto.EvaluacionCriterioReglaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class Criterio402HistorialCentralRiesgoService {

    private static final String CODIGO_CRITERIO = "402";

    private static final BigDecimal VALOR_SIN_INFORMACION =
            BigDecimal.valueOf(-1);

    private final EvaluacionReglasService evaluacionReglasService;

    // =========================================================
    // EVALUAR CON CENTRAL DE RIESGO
    // =========================================================

    public EvaluacionCriterioResultadoDTO evaluar(
            Criterio402DatoDTO dato,
            LocalDate fechaCorte
    ) {

        validarSolicitud(
                dato,
                fechaCorte
        );

        String calificacionNormalizada =
                normalizarCalificacion(
                        dato.getCalificacionCentralRiesgo()
                );

        BigDecimal valorResultado =
                convertirCalificacionAValor(
                        calificacionNormalizada,
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
                calificacionNormalizada,
                valorResultado,
                regla
        );
    }

    // =========================================================
    // EVALUAR SIN CENTRAL DE RIESGO
    //
    // Cuando no existe archivo de Central:
    //
    // - se utiliza edad_de_mora del cierre;
    // - F se trata como E.
    // =========================================================

    public EvaluacionCriterioResultadoDTO evaluarSinCentral(
            EvaluacionCreditoDTO credito,
            LocalDate fechaCorte
    ) {

        validarSolicitudSinCentral(
                credito,
                fechaCorte
        );

        String edadMora =
                normalizarEdadMora(
                        credito.getEdadMora()
                );

        BigDecimal valorResultado =
                convertirEdadMoraAValor(
                        edadMora
                );

        EvaluacionCriterioReglaDTO regla =
                evaluacionReglasService.buscarReglaAplicable(
                        CODIGO_CRITERIO,
                        valorResultado,
                        null
                );

        return construirResultadoSinCentral(
                credito,
                fechaCorte,
                edadMora,
                valorResultado,
                regla
        );
    }

    // =========================================================
    // CONVERTIR CALIFICACIÓN CENTRAL A VALOR
    // =========================================================

    private BigDecimal convertirCalificacionAValor(
            String calificacion,
            Criterio402DatoDTO dato
    ) {

        if (calificacion == null) {
            return VALOR_SIN_INFORMACION;
        }

        return switch (calificacion) {

            case "A" ->
                    BigDecimal.valueOf(1);

            case "B" ->
                    BigDecimal.valueOf(2);

            case "C" ->
                    BigDecimal.valueOf(3);

            case "D" ->
                    BigDecimal.valueOf(4);

            case "E" ->
                    BigDecimal.valueOf(5);

            case "F" ->
                    BigDecimal.valueOf(6);

            case "CASTIGADA",
                 "CASTIGADO",
                 "CARTERA CASTIGADA" ->
                    BigDecimal.valueOf(7);

            default ->
                    throw new IllegalArgumentException(
                            "La calificación de central de riesgo '"
                                    + calificacion
                                    + "' del crédito "
                                    + valorCredito(
                                    dato.getIdCarteraCredito()
                            )
                                    + " no está soportada por el criterio 402."
                    );
        };
    }

    // =========================================================
    // CONVERTIR EDAD DE MORA A VALOR
    // =========================================================

    private BigDecimal convertirEdadMoraAValor(
            String edadMora
    ) {

        return switch (edadMora) {

            case "A" ->
                    BigDecimal.valueOf(1);

            case "B" ->
                    BigDecimal.valueOf(2);

            case "C" ->
                    BigDecimal.valueOf(3);

            case "D" ->
                    BigDecimal.valueOf(4);

            case "E" ->
                    BigDecimal.valueOf(5);

            default ->
                    throw new IllegalArgumentException(
                            "La edad de mora "
                                    + edadMora
                                    + " no es válida para el criterio 402."
                    );
        };
    }

    // =========================================================
    // CONSTRUIR RESULTADO CON CENTRAL
    // =========================================================

    private EvaluacionCriterioResultadoDTO construirResultado(
            Criterio402DatoDTO dato,
            LocalDate fechaCorte,
            String calificacion,
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

        completarResultadoRegla(
                resultado,
                valorResultado,
                regla
        );

        resultado.setObservaciones(
                construirObservacion(
                        dato,
                        fechaCorte,
                        calificacion,
                        regla
                )
        );

        return resultado;
    }

    // =========================================================
    // CONSTRUIR RESULTADO SIN CENTRAL
    // =========================================================

    private EvaluacionCriterioResultadoDTO construirResultadoSinCentral(
            EvaluacionCreditoDTO credito,
            LocalDate fechaCorte,
            String edadMora,
            BigDecimal valorResultado,
            EvaluacionCriterioReglaDTO regla
    ) {

        EvaluacionCriterioResultadoDTO resultado =
                new EvaluacionCriterioResultadoDTO();

        resultado.setIdCierreCarteraCredito(
                credito.getIdCierreCarteraCredito()
        );

        resultado.setIdCarteraCredito(
                credito.getIdCarteraCredito()
        );

        resultado.setIdDatosPersonal(
                credito.getIdDatosPersonal()
        );

        resultado.setDocumento(
                credito.getDocumento()
        );

        completarResultadoRegla(
                resultado,
                valorResultado,
                regla
        );

        resultado.setObservaciones(
                construirObservacionSinCentral(
                        credito,
                        fechaCorte,
                        edadMora,
                        regla
                )
        );

        return resultado;
    }

    // =========================================================
    // COMPLETAR RESULTADO DE REGLA
    // =========================================================

    private void completarResultadoRegla(
            EvaluacionCriterioResultadoDTO resultado,
            BigDecimal valorResultado,
            EvaluacionCriterioReglaDTO regla
    ) {

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
    }

    // =========================================================
    // OBSERVACIÓN CON CENTRAL
    // =========================================================

    private String construirObservacion(
            Criterio402DatoDTO dato,
            LocalDate fechaCorte,
            String calificacion,
            EvaluacionCriterioReglaDTO regla
    ) {

        if (calificacion == null) {

            return "Fecha de corte: "
                    + fechaCorte
                    + ". Documento: "
                    + valorDocumento(
                    dato.getDocumento()
            )
                    + ". Clasificación del crédito: "
                    + valorClasificacion(
                    dato.getCodigoClasificacionCredito()
            )
                    + ". No existe información coincidente en el archivo "
                    + "de la Central de Riesgo. Regla aplicada: "
                    + regla.getNombreRegla()
                    + ".";
        }

        return "Fecha de corte: "
                + fechaCorte
                + ". Archivo: "
                + valorArchivo(
                dato.getNombreArchivoCentral()
        )
                + ". Documento: "
                + valorDocumento(
                dato.getDocumento()
        )
                + ". Clasificación del crédito: "
                + valorClasificacion(
                dato.getCodigoClasificacionCredito()
        )
                + ". Calificación reportada: "
                + calificacion
                + ". Regla aplicada: "
                + regla.getNombreRegla()
                + ".";
    }

    // =========================================================
    // OBSERVACIÓN SIN CENTRAL
    // =========================================================

    private String construirObservacionSinCentral(
            EvaluacionCreditoDTO credito,
            LocalDate fechaCorte,
            String edadMora,
            EvaluacionCriterioReglaDTO regla
    ) {

        return "Fecha de corte: "
                + fechaCorte
                + ". Documento: "
                + valorDocumento(
                credito.getDocumento()
        )
                + ". Clasificación del crédito: "
                + valorClasificacion(
                credito.getCodigoClasificacionCredito()
        )
                + ". Evaluación ejecutada sin archivo de Central de Riesgo. "
                + "Se utilizó la edad de mora del cierre: "
                + edadMora
                + ". Regla aplicada: "
                + regla.getNombreRegla()
                + ".";
    }

    // =========================================================
    // VALIDAR SOLICITUD CON CENTRAL
    // =========================================================

    private void validarSolicitud(
            Criterio402DatoDTO dato,
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

        validarDocumento(
                dato.getDocumento()
        );

        validarClasificacion(
                dato.getCodigoClasificacionCredito(),
                dato.getIdCarteraCredito()
        );
    }

    // =========================================================
    // VALIDAR SOLICITUD SIN CENTRAL
    // =========================================================

    private void validarSolicitudSinCentral(
            EvaluacionCreditoDTO credito,
            LocalDate fechaCorte
    ) {

        if (credito == null) {
            throw new IllegalArgumentException(
                    "Los datos del crédito a evaluar son obligatorios."
            );
        }

        if (credito.getIdCierreCarteraCredito() == null) {
            throw new IllegalArgumentException(
                    "El identificador del crédito del cierre es obligatorio."
            );
        }

        if (credito.getIdCarteraCredito() == null) {
            throw new IllegalArgumentException(
                    "El identificador del crédito es obligatorio."
            );
        }

        if (credito.getIdDatosPersonal() == null) {
            throw new IllegalArgumentException(
                    "El identificador de la persona es obligatorio."
            );
        }

        if (fechaCorte == null) {
            throw new IllegalArgumentException(
                    "La fecha de corte de la evaluación es obligatoria."
            );
        }

        validarDocumento(
                credito.getDocumento()
        );

        validarClasificacion(
                credito.getCodigoClasificacionCredito(),
                credito.getIdCarteraCredito()
        );

        if (
                credito.getEdadMora() == null
                        || credito.getEdadMora().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "La edad de mora del crédito "
                            + valorCredito(
                            credito.getIdCarteraCredito()
                    )
                            + " es obligatoria cuando la evaluación "
                            + "se ejecuta sin Central de Riesgo."
            );
        }
    }

    // =========================================================
    // VALIDAR DOCUMENTO
    // =========================================================

    private void validarDocumento(
            String documento
    ) {

        if (
                documento == null
                        || documento.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "El documento es obligatorio para evaluar "
                            + "el criterio 402."
            );
        }
    }

    // =========================================================
    // VALIDAR CLASIFICACIÓN
    // =========================================================

    private void validarClasificacion(
            String clasificacion,
            Integer idCarteraCredito
    ) {

        if (
                clasificacion == null
                        || clasificacion.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "La clasificación del crédito "
                            + valorCredito(
                            idCarteraCredito
                    )
                            + " es obligatoria para evaluar "
                            + "el criterio 402."
            );
        }
    }

    // =========================================================
    // NORMALIZAR CALIFICACIÓN CENTRAL
    // =========================================================

    private String normalizarCalificacion(
            String calificacion
    ) {

        if (calificacion == null) {
            return null;
        }

        String resultado =
                calificacion
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        return resultado.isEmpty()
                ? null
                : resultado;
    }

    // =========================================================
    // NORMALIZAR EDAD DE MORA
    // =========================================================

    private String normalizarEdadMora(
            String edadMora
    ) {

        if (
                edadMora == null
                        || edadMora.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "La edad de mora es obligatoria."
            );
        }

        String valor =
                edadMora
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if ("F".equals(valor)) {
            return "E";
        }

        if (
                !"A".equals(valor)
                        && !"B".equals(valor)
                        && !"C".equals(valor)
                        && !"D".equals(valor)
                        && !"E".equals(valor)
        ) {
            throw new IllegalArgumentException(
                    "La edad de mora "
                            + valor
                            + " no es válida para el criterio 402."
            );
        }

        return valor;
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

    private String valorClasificacion(
            String clasificacion
    ) {

        if (
                clasificacion == null
                        || clasificacion.isBlank()
        ) {
            return "SIN CLASIFICACIÓN";
        }

        return clasificacion.trim();
    }

    private String valorArchivo(
            String nombreArchivo
    ) {

        if (
                nombreArchivo == null
                        || nombreArchivo.isBlank()
        ) {
            return "SIN ARCHIVO";
        }

        return nombreArchivo.trim();
    }
}