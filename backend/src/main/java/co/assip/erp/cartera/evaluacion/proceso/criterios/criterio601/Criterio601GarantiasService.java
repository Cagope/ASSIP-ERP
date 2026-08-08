package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio601;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio601.dto.Criterio601DatoDTO;
import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCriterioResultadoDTO;
import co.assip.erp.cartera.evaluacion.proceso.reglas.EvaluacionReglasService;
import co.assip.erp.cartera.evaluacion.reglas.dto.EvaluacionCriterioReglaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class Criterio601GarantiasService {

    private static final String CODIGO_CRITERIO = "601";

    private final EvaluacionReglasService evaluacionReglasService;

    // =========================================================
    // EVALUAR CRITERIO 601 - GARANTÍAS
    // =========================================================

    public EvaluacionCriterioResultadoDTO evaluar(
            Criterio601DatoDTO dato,
            LocalDate fechaCorte
    ) {

        validarSolicitud(
                dato,
                fechaCorte
        );

        BigDecimal valorResultado =
                convertirCodigoGarantia(
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
    // CONVERTIR CÓDIGO DE GARANTÍA
    // =========================================================

    private BigDecimal convertirCodigoGarantia(
            Criterio601DatoDTO dato
    ) {

        String codigo =
                dato.getCodigoGarantiaCredito();

        if (
                codigo == null
                        || codigo.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "El crédito "
                            + valorCredito(
                            dato.getIdCarteraCredito()
                    )
                            + " no tiene código de garantía "
                            + "en la fotografía histórica."
            );
        }

        String codigoNormalizado =
                codigo.trim();

        try {
            return new BigDecimal(
                    codigoNormalizado
            );
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "El código de garantía '"
                            + codigoNormalizado
                            + "' del crédito "
                            + valorCredito(
                            dato.getIdCarteraCredito()
                    )
                            + " no es numérico y no puede ser evaluado "
                            + "por el criterio 601."
            );
        }
    }

    // =========================================================
    // CONSTRUIR RESULTADO
    // =========================================================

    private EvaluacionCriterioResultadoDTO construirResultado(
            Criterio601DatoDTO dato,
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
            Criterio601DatoDTO dato,
            LocalDate fechaCorte,
            EvaluacionCriterioReglaDTO regla
    ) {

        return "Fecha de corte: "
                + fechaCorte
                + ". Código de garantía: "
                + valorCodigoGarantia(
                dato.getCodigoGarantiaCredito()
        )
                + ". Garantía: "
                + valorDescripcionGarantia(
                dato.getDescripcionGarantiaCredito()
        )
                + ". Tipo de garantía: "
                + valorTipoGarantia(
                dato.getTipoGarantia()
        )
                + ". Regla aplicada: "
                + regla.getNombreRegla()
                + ".";
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarSolicitud(
            Criterio601DatoDTO dato,
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

        if (
                dato.getCodigoGarantiaCredito() == null
                        || dato.getCodigoGarantiaCredito().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "El crédito "
                            + valorCredito(
                            dato.getIdCarteraCredito()
                    )
                            + " no tiene garantía registrada "
                            + "en la fotografía histórica."
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

    private String valorCodigoGarantia(
            String codigo
    ) {

        if (
                codigo == null
                        || codigo.isBlank()
        ) {
            return "SIN CÓDIGO";
        }

        return codigo.trim();
    }

    private String valorDescripcionGarantia(
            String descripcion
    ) {

        if (
                descripcion == null
                        || descripcion.isBlank()
        ) {
            return "SIN DESCRIPCIÓN";
        }

        return descripcion.trim();
    }

    private String valorTipoGarantia(
            String tipoGarantia
    ) {

        if (
                tipoGarantia == null
                        || tipoGarantia.isBlank()
        ) {
            return "SIN TIPO";
        }

        return tipoGarantia.trim();
    }
}