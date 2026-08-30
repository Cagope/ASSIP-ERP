package co.assip.erp.cartera.analisis.matrizrodamiento.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class MatrizRodamientoFilaDTO {

    // =========================================================
    // CATEGORÍA DE ORIGEN
    // =========================================================

    /**
     * Categoría del corte anterior.
     *
     * A, B, C, D o E.
     */
    private String categoriaAnterior;

    // =========================================================
    // CELDAS A -> A ... E
    // =========================================================

    private List<MatrizRodamientoCeldaDTO> celdas =
            new ArrayList<>();

    // =========================================================
    // TOTALES DE FILA
    // =========================================================

    private Integer totalCantidad;

    private BigDecimal totalValor;

    // =========================================================
    // PROBABILIDADES POR CANTIDADES
    // =========================================================

    private BigDecimal probabilidadMejoraCantidad;

    private BigDecimal probabilidadPermanenciaCantidad;

    private BigDecimal probabilidadDeterioroCantidad;

    // =========================================================
    // PROBABILIDADES POR VALORES
    // =========================================================

    private BigDecimal probabilidadMejoraValor;

    private BigDecimal probabilidadPermanenciaValor;

    private BigDecimal probabilidadDeterioroValor;
}