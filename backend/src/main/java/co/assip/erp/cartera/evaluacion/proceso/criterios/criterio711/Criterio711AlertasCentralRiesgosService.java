package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio711;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio711.dto.Criterio711DatoDTO;
import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCreditoDTO;
import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCriterioResultadoDTO;
import co.assip.erp.cartera.evaluacion.proceso.reglas.EvaluacionReglasService;
import co.assip.erp.cartera.evaluacion.reglas.dto.EvaluacionCriterioReglaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class Criterio711AlertasCentralRiesgosService {

    private static final String CODIGO_CRITERIO = "711";

    private static final BigDecimal VALOR_SIN_INFORMACION =
            BigDecimal.valueOf(-1);

    private static final BigDecimal VALOR_SIN_CENTRAL =
            BigDecimal.ZERO;

    private final EvaluacionReglasService evaluacionReglasService;

    // =========================================================
    // EVALUAR CON CENTRAL DE RIESGO
    // =========================================================

    public EvaluacionCriterioResultadoDTO evaluar(
            Criterio711DatoDTO dato,
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
    // EVALUAR SIN CENTRAL DE RIESGO
    //
    // Si la evaluación se ejecuta sin archivo de Central,
    // se consideran 0 alertas.
    // =========================================================

    public EvaluacionCriterioResultadoDTO evaluarSinCentral(
            EvaluacionCreditoDTO credito,
            LocalDate fechaCorte
    ) {

        validarSolicitudSinCentral(
                credito,
                fechaCorte
        );

        BigDecimal valorResultado =
                VALOR_SIN_CENTRAL;

        EvaluacionCriterioReglaDTO regla =
                evaluacionReglasService.buscarReglaAplicable(
                        CODIGO_CRITERIO,
                        valorResultado,
                        null
                );

        return construirResultadoSinCentral(
                credito,
                fechaCorte,
                valorResultado,
                regla
        );
    }

    // =========================================================
    // DETERMINAR VALOR RESULTADO CON CENTRAL
    // =========================================================

    private BigDecimal determinarValorResultado(
            Criterio711DatoDTO dato
    ) {

        Integer alertasTotales =
                dato.getAlertasTotales();

        if (alertasTotales == null) {
            return VALOR_SIN_INFORMACION;
        }

        if (alertasTotales < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de alertas del crédito "
                            + valorCredito(
                            dato.getIdCarteraCredito()
                    )
                            + " no puede ser negativa."
            );
        }

        return BigDecimal.valueOf(
                alertasTotales
        );
    }

    // =========================================================
    // CONSTRUIR RESULTADO CON CENTRAL
    // =========================================================

    private EvaluacionCriterioResultadoDTO construirResultado(
            Criterio711DatoDTO dato,
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

        completarResultadoRegla(
                resultado,
                valorResultado,
                regla
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
    // CONSTRUIR RESULTADO SIN CENTRAL
    // =========================================================

    private EvaluacionCriterioResultadoDTO construirResultadoSinCentral(
            EvaluacionCreditoDTO credito,
            LocalDate fechaCorte,
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
            Criterio711DatoDTO dato,
            LocalDate fechaCorte,
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
                    + ". Documento: "
                    + valorDocumento(
                    dato.getDocumento()
            )
                    + ". Clasificación del crédito: "
                    + valorClasificacion(
                    dato.getCodigoClasificacionCredito()
            )
                    + ". No existe información utilizable de alertas "
                    + "en el archivo de la Central de Riesgo. "
                    + "Regla aplicada: "
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
                + ". Alertas reportadas: "
                + dato.getAlertasTotales()
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
                + "Se asumieron 0 alertas. "
                + "Regla aplicada: "
                + regla.getNombreRegla()
                + ".";
    }

    // =========================================================
    // VALIDAR SOLICITUD CON CENTRAL
    // =========================================================

    private void validarSolicitud(
            Criterio711DatoDTO dato,
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
                            + "el criterio 711."
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
                            + "el criterio 711."
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