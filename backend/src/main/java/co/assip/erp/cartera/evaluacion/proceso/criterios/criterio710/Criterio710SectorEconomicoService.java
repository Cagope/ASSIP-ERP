package co.assip.erp.cartera.evaluacion.proceso.criterios.criterio710;

import co.assip.erp.cartera.evaluacion.proceso.criterios.criterio710.dto.Criterio710DatoDTO;
import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCriterioResultadoDTO;
import co.assip.erp.cartera.evaluacion.proceso.reglas.EvaluacionReglasService;
import co.assip.erp.cartera.evaluacion.reglas.dto.EvaluacionCriterioReglaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class Criterio710SectorEconomicoService {

    private static final String CODIGO_CRITERIO = "710";

    private final EvaluacionReglasService evaluacionReglasService;

    // =========================================================
    // EVALUAR CRITERIO 710 - SECTOR ECONÓMICO
    // =========================================================

    public EvaluacionCriterioResultadoDTO evaluar(
            Criterio710DatoDTO dato,
            LocalDate fechaCorte
    ) {

        validarSolicitud(
                dato,
                fechaCorte
        );

        BigDecimal valorResultado =
                convertirCodigoSector(
                        dato
                );

        /*
         * El criterio no conoce códigos de reglas.
         *
         * Solo entrega el valor técnico obtenido de la fotografía
         * histórica y el selector determina dinámicamente qué regla
         * activa corresponde.
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
    // CONVERTIR CÓDIGO DE SECTOR ECONÓMICO
    // =========================================================

    private BigDecimal convertirCodigoSector(
            Criterio710DatoDTO dato
    ) {

        String codigo =
                dato.getCodigoSectorEconomico();

        if (
                codigo == null
                        || codigo.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "La persona "
                            + valorDocumento(
                            dato.getDocumento()
                    )
                            + ", crédito "
                            + valorCredito(
                            dato.getIdCarteraCredito()
                    )
                            + ", no tiene código de sector económico "
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
                    "El código de sector económico '"
                            + codigoNormalizado
                            + "' de la persona "
                            + valorDocumento(
                            dato.getDocumento()
                    )
                            + ", crédito "
                            + valorCredito(
                            dato.getIdCarteraCredito()
                    )
                            + ", no es numérico y no puede ser evaluado "
                            + "por el criterio 710."
            );
        }
    }

    // =========================================================
    // CONSTRUIR RESULTADO
    // =========================================================

    private EvaluacionCriterioResultadoDTO construirResultado(
            Criterio710DatoDTO dato,
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
            Criterio710DatoDTO dato,
            LocalDate fechaCorte,
            EvaluacionCriterioReglaDTO regla
    ) {

        return "Fecha de corte: "
                + fechaCorte
                + ". Código de sector económico: "
                + valorCodigoSector(
                dato.getCodigoSectorEconomico()
        )
                + ". Regla aplicada: "
                + regla.getNombreRegla()
                + ".";
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarSolicitud(
            Criterio710DatoDTO dato,
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
                dato.getCodigoSectorEconomico() == null
                        || dato.getCodigoSectorEconomico().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "La persona "
                            + valorDocumento(
                            dato.getDocumento()
                    )
                            + ", crédito "
                            + valorCredito(
                            dato.getIdCarteraCredito()
                    )
                            + ", no tiene sector económico registrado "
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

    private String valorCodigoSector(
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
}