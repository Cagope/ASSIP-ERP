package co.assip.erp.cartera.evaluacion.proceso.reglas;

import co.assip.erp.cartera.evaluacion.reglas.dto.EvaluacionCriterioReglaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvaluacionReglasService {

    private final EvaluacionReglasRepository repository;

    // =========================================================
    // BUSCAR REGLA APLICABLE
    // =========================================================

    public EvaluacionCriterioReglaDTO buscarReglaAplicable(
            String codigoCriterio,
            BigDecimal valorResultado,
            Short idTipoPersona
    ) {

        validarSolicitud(
                codigoCriterio,
                valorResultado
        );

        List<EvaluacionCriterioReglaDTO> reglas =
                repository.listarReglasActivas(
                        codigoCriterio,
                        idTipoPersona
                );

        if (reglas.isEmpty()) {
            throw new IllegalArgumentException(
                    "No existen reglas activas y aplicables "
                            + "para el criterio "
                            + codigoCriterio
                            + "."
            );
        }

        for (EvaluacionCriterioReglaDTO regla : reglas) {

            if (
                    aplicaRegla(
                            regla,
                            valorResultado
                    )
            ) {
                return regla;
            }
        }

        throw new IllegalArgumentException(
                "Ninguna regla activa del criterio "
                        + codigoCriterio
                        + " aplica al valor "
                        + valorResultado
                        .stripTrailingZeros()
                        .toPlainString()
                        + "."
        );
    }

    // =========================================================
    // VALIDAR SI UNA REGLA APLICA
    // =========================================================

    private boolean aplicaRegla(
            EvaluacionCriterioReglaDTO regla,
            BigDecimal valorResultado
    ) {

        if (regla == null) {
            return false;
        }

        String tipoRegla =
                regla.getTipoRegla();

        if ("C".equals(tipoRegla)) {
            return aplicaComparacionExacta(
                    regla,
                    valorResultado
            );
        }

        if ("R".equals(tipoRegla)) {
            return aplicaRango(
                    regla,
                    valorResultado
            );
        }

        throw new IllegalArgumentException(
                "La regla "
                        + regla.getCodigoRegla()
                        + " del criterio "
                        + regla.getCodigoCriterio()
                        + " tiene un tipo no válido: "
                        + tipoRegla
                        + "."
        );
    }

    // =========================================================
    // COMPARACIÓN EXACTA
    // =========================================================

    private boolean aplicaComparacionExacta(
            EvaluacionCriterioReglaDTO regla,
            BigDecimal valorResultado
    ) {

        BigDecimal valorComparacion =
                regla.getValorComparacion();

        if (valorComparacion == null) {
            throw new IllegalArgumentException(
                    "La regla "
                            + regla.getCodigoRegla()
                            + " no tiene valor de comparación."
            );
        }

        return valorResultado.compareTo(
                valorComparacion
        ) == 0;
    }

    // =========================================================
    // COMPARACIÓN POR RANGO
    // =========================================================

    private boolean aplicaRango(
            EvaluacionCriterioReglaDTO regla,
            BigDecimal valorResultado
    ) {

        BigDecimal valorDesde =
                regla.getValorDesde();

        BigDecimal valorHasta =
                regla.getValorHasta();

        if (
                valorDesde == null
                        && valorHasta == null
        ) {
            throw new IllegalArgumentException(
                    "La regla "
                            + regla.getCodigoRegla()
                            + " no tiene límites configurados."
            );
        }

        if (
                valorDesde != null
                        && valorResultado.compareTo(
                        valorDesde
                ) < 0
        ) {
            return false;
        }

        if (
                valorHasta != null
                        && valorResultado.compareTo(
                        valorHasta
                ) > 0
        ) {
            return false;
        }

        return true;
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarSolicitud(
            String codigoCriterio,
            BigDecimal valorResultado
    ) {

        if (
                codigoCriterio == null
                        || codigoCriterio.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "El código del criterio es obligatorio."
            );
        }

        if (valorResultado == null) {
            throw new IllegalArgumentException(
                    "El valor resultado del criterio es obligatorio."
            );
        }
    }
}