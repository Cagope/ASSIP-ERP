package co.assip.erp.cartera.evaluacion.proceso.motor;

import co.assip.erp.cartera.evaluacion.proceso.motor.dto.EvaluacionEdadRiesgoResultadoDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EvaluacionEdadRiesgoService {

    private static final String CATEGORIA_A = "A";
    private static final String CATEGORIA_B = "B";
    private static final String CATEGORIA_C = "C";
    private static final String CATEGORIA_D = "D";
    private static final String CATEGORIA_E = "E";

    // =========================================================
    // CALCULAR RESULTADO INDIVIDUAL
    // =========================================================

    public EvaluacionEdadRiesgoResultadoDTO calcular(
            BigDecimal puntajeTotal,
            BigDecimal puntajeMinimoFavorable,
            String edadRiesgoAnterior,
            String edadMora,
            Boolean creditoEvaluado
    ) {

        validarSolicitud(
                puntajeTotal,
                puntajeMinimoFavorable,
                edadRiesgoAnterior,
                edadMora,
                creditoEvaluado
        );

        String riesgoActual =
                normalizarCategoria(
                        edadRiesgoAnterior
                );

        String mora =
                normalizarCategoria(
                        edadMora
                );

        /*
         * La evaluación parte de la edad de riesgo vigente
         * que trae el cierre.
         *
         * La mora actúa como piso:
         * la recomendación nunca puede quedar mejor que ella.
         */
        String edadBase =
                peorCategoria(
                        riesgoActual,
                        mora
                );

        boolean resultadoFavorable =
                puntajeTotal.compareTo(
                        puntajeMinimoFavorable
                ) >= 0;

        if (!resultadoFavorable) {

            return calcularDeterioro(
                    puntajeTotal,
                    puntajeMinimoFavorable,
                    riesgoActual,
                    mora,
                    edadBase
            );
        }

        return calcularResultadoFavorable(
                puntajeTotal,
                puntajeMinimoFavorable,
                riesgoActual,
                mora,
                edadBase,
                creditoEvaluado
        );
    }

    // =========================================================
// RESULTADO DESFAVORABLE
// =========================================================

    private EvaluacionEdadRiesgoResultadoDTO calcularDeterioro(
            BigDecimal puntajeTotal,
            BigDecimal puntajeMinimoFavorable,
            String riesgoActual,
            String mora,
            String edadBase
    ) {

        String edadCalculada =
                aumentarCategoria(
                        edadBase
                );

        String edadArrastre =
                peorCategoria(
                        edadCalculada,
                        mora
                );

        return new EvaluacionEdadRiesgoResultadoDTO(
                edadCalculada,
                edadArrastre,
                construirComentarioDeterioro(
                        puntajeTotal,
                        puntajeMinimoFavorable,
                        riesgoActual,
                        mora,
                        edadBase,
                        edadArrastre
                )
        );
    }

    // =========================================================
// RESULTADO FAVORABLE
// =========================================================

    private EvaluacionEdadRiesgoResultadoDTO calcularResultadoFavorable(
            BigDecimal puntajeTotal,
            BigDecimal puntajeMinimoFavorable,
            String riesgoActual,
            String mora,
            String edadBase,
            Boolean creditoEvaluado
    ) {

        if (!Boolean.TRUE.equals(creditoEvaluado)) {

            return new EvaluacionEdadRiesgoResultadoDTO(
                    edadBase,
                    edadBase,
                    construirComentarioMantenerNoEvaluado(
                            puntajeTotal,
                            puntajeMinimoFavorable,
                            riesgoActual,
                            mora,
                            edadBase
                    )
            );
        }

        String edadCalculada =
                disminuirCategoria(
                        edadBase
                );

        String edadArrastre =
                peorCategoria(
                        edadCalculada,
                        mora
                );

        return new EvaluacionEdadRiesgoResultadoDTO(
                edadCalculada,
                edadArrastre,
                construirComentarioMejora(
                        puntajeTotal,
                        puntajeMinimoFavorable,
                        riesgoActual,
                        mora,
                        edadBase,
                        edadCalculada,
                        edadArrastre
                )
        );
    }

    // =========================================================
    // AUMENTAR CATEGORÍA
    //
    // A -> B
    // B -> C
    // C -> D
    // D -> E
    // E -> E
    // =========================================================

    public String aumentarCategoria(
            String categoria
    ) {

        String valor =
                normalizarCategoria(
                        categoria
                );

        return switch (valor) {

            case CATEGORIA_A -> CATEGORIA_B;
            case CATEGORIA_B -> CATEGORIA_C;
            case CATEGORIA_C -> CATEGORIA_D;

            case CATEGORIA_D,
                 CATEGORIA_E -> CATEGORIA_E;

            default -> throw new IllegalArgumentException(
                    "La categoría de riesgo "
                            + valor
                            + " no es válida."
            );
        };
    }

    // =========================================================
    // DISMINUIR CATEGORÍA
    //
    // A -> A
    // B -> A
    // C -> B
    // D -> C
    // E -> D
    // =========================================================

    public String disminuirCategoria(
            String categoria
    ) {

        String valor =
                normalizarCategoria(
                        categoria
                );

        return switch (valor) {

            case CATEGORIA_A -> CATEGORIA_A;
            case CATEGORIA_B -> CATEGORIA_A;
            case CATEGORIA_C -> CATEGORIA_B;
            case CATEGORIA_D -> CATEGORIA_C;
            case CATEGORIA_E -> CATEGORIA_D;

            default -> throw new IllegalArgumentException(
                    "La categoría de riesgo "
                            + valor
                            + " no es válida."
            );
        };
    }

    // =========================================================
    // OBTENER PEOR CATEGORÍA
    // =========================================================

    public String peorCategoria(
            String categoriaUno,
            String categoriaDos
    ) {

        String uno =
                normalizarCategoria(
                        categoriaUno
                );

        String dos =
                normalizarCategoria(
                        categoriaDos
                );

        return valorCategoria(uno)
                >= valorCategoria(dos)
                ? uno
                : dos;
    }

    // =========================================================
    // COMPARAR CATEGORÍAS
    //
    // < 0 = primera mejor
    //   0 = iguales
    // > 0 = primera peor
    // =========================================================

    public int compararCategorias(
            String categoriaUno,
            String categoriaDos
    ) {

        String uno =
                normalizarCategoria(
                        categoriaUno
                );

        String dos =
                normalizarCategoria(
                        categoriaDos
                );

        return Integer.compare(
                valorCategoria(uno),
                valorCategoria(dos)
        );
    }

    // =========================================================
    // VALOR ORDINAL
    // =========================================================

    private int valorCategoria(
            String categoria
    ) {

        return switch (categoria) {

            case CATEGORIA_A -> 1;
            case CATEGORIA_B -> 2;
            case CATEGORIA_C -> 3;
            case CATEGORIA_D -> 4;
            case CATEGORIA_E -> 5;

            default -> throw new IllegalArgumentException(
                    "La categoría de riesgo "
                            + categoria
                            + " no es válida."
            );
        };
    }

    // =========================================================
    // NORMALIZAR CATEGORÍA
    // =========================================================

    public String normalizarCategoria(
            String categoria
    ) {

        if (
                categoria == null
                        || categoria.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "La categoría de riesgo es obligatoria."
            );
        }

        String valor =
                categoria.trim().toUpperCase();

        /*
         * Regla funcional:
         *
         * La categoría F no participa como una edad independiente
         * dentro del motor de evaluación.
         *
         * Todo valor F se trata como categoría E.
         */
        if ("F".equals(valor)) {
            return CATEGORIA_E;
        }

        if (
                !CATEGORIA_A.equals(valor)
                        && !CATEGORIA_B.equals(valor)
                        && !CATEGORIA_C.equals(valor)
                        && !CATEGORIA_D.equals(valor)
                        && !CATEGORIA_E.equals(valor)
        ) {
            throw new IllegalArgumentException(
                    "La categoría de riesgo "
                            + valor
                            + " no es válida. "
                            + "Se esperaba A, B, C, D, E o F."
            );
        }

        return valor;
    }

    // =========================================================
// VALIDACIONES
// =========================================================

    private void validarSolicitud(
            BigDecimal puntajeTotal,
            BigDecimal puntajeMinimoFavorable,
            String edadRiesgoAnterior,
            String edadMora,
            Boolean creditoEvaluado
    ) {

        if (puntajeTotal == null) {
            throw new IllegalArgumentException(
                    "El puntaje total de la evaluación es obligatorio."
            );
        }

        if (
                puntajeTotal.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new IllegalArgumentException(
                    "El puntaje total de la evaluación "
                            + "no puede ser negativo."
            );
        }

        if (puntajeMinimoFavorable == null) {
            throw new IllegalArgumentException(
                    "El parámetro 210 — Puntaje mínimo de resultado "
                            + "favorable no está configurado."
            );
        }

        if (
                puntajeMinimoFavorable.compareTo(
                        BigDecimal.ZERO
                ) < 0
        ) {
            throw new IllegalArgumentException(
                    "El parámetro 210 — Puntaje mínimo de resultado "
                            + "favorable no es válido."
            );
        }

        if (creditoEvaluado == null) {
            throw new IllegalArgumentException(
                    "El indicador credito_evaluado del cierre "
                            + "es obligatorio."
            );
        }

        normalizarCategoria(
                edadRiesgoAnterior
        );

        normalizarCategoria(
                edadMora
        );
    }

    // =========================================================
    // COMENTARIOS
    // =========================================================

    private String construirComentarioDeterioro(
            BigDecimal puntajeTotal,
            BigDecimal puntajeMinimo,
            String riesgoActual,
            String mora,
            String edadBase,
            String edadArrastre
    ) {

        return "Puntaje total "
                + numero(puntajeTotal)
                + ", inferior al mínimo favorable "
                + numero(puntajeMinimo)
                + ". Edad de riesgo actual: "
                + riesgoActual
                + ". Edad por mora: "
                + mora
                + ". Edad base: "
                + edadBase
                + ". Se sugiere aumentar una categoría. "
                + "Edad individual sugerida: "
                + edadArrastre
                + ".";
    }

    private String construirComentarioMantenerNoEvaluado(
            BigDecimal puntajeTotal,
            BigDecimal puntajeMinimo,
            String riesgoActual,
            String mora,
            String edadBase
    ) {

        return "Puntaje total "
                + numero(puntajeTotal)
                + ", igual o superior al mínimo favorable "
                + numero(puntajeMinimo)
                + ". Edad de riesgo actual: "
                + riesgoActual
                + ". Edad por mora: "
                + mora
                + ". El crédito no venía evaluado en el cierre, "
                + "por lo tanto no se sugiere disminuir la categoría. "
                + "Edad individual sugerida: "
                + edadBase
                + ".";
    }

    private String construirComentarioMejora(
            BigDecimal puntajeTotal,
            BigDecimal puntajeMinimo,
            String riesgoActual,
            String mora,
            String edadBase,
            String edadCalculada,
            String edadArrastre
    ) {

        return "Puntaje total "
                + numero(puntajeTotal)
                + ", igual o superior al mínimo favorable "
                + numero(puntajeMinimo)
                + ". Edad de riesgo actual: "
                + riesgoActual
                + ". Edad por mora: "
                + mora
                + ". Edad base: "
                + edadBase
                + ". El crédito venía evaluado y se sugiere "
                + "disminuir una categoría a "
                + edadCalculada
                + ". Edad individual sugerida después "
                + "del control de mora: "
                + edadArrastre
                + ".";
    }

    // =========================================================
    // APOYO
    // =========================================================

    private String numero(
            BigDecimal valor
    ) {

        return valor
                .stripTrailingZeros()
                .toPlainString();
    }
}