package co.assip.erp.cartera.analisis.matrizrodamiento.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class MatrizRodamientoDTO {

    // =========================================================
    // PARÁMETROS DEL PROCESO
    // =========================================================

    /**
     * ACTUAL o CORTE.
     */
    private String tipoPartida;

    /**
     * Para ACTUAL corresponde a la fecha actual.
     *
     * Para CORTE corresponde al cierre seleccionado
     * como datos de partida.
     */
    private LocalDate fechaPartida;

    /**
     * Corte histórico anterior contra el cual
     * se comparan los datos de partida.
     */
    private LocalDate fechaComparacion;

    // =========================================================
    // CONTROLES
    // =========================================================

    /**
     * Créditos activos con saldo > 0
     * existentes en los datos de partida.
     */
    private Integer cantidadPoblacionPartida;

    /**
     * Créditos de la población de partida que
     * también fueron encontrados en el corte anterior.
     *
     * Este es el universo efectivo de la matriz,
     * igual que en el VB.
     */
    private Integer cantidadCreditosMatriz;

    // =========================================================
    // MATRIZ
    // =========================================================

    private List<MatrizRodamientoFilaDTO> filas =
            new ArrayList<>();

    // =========================================================
    // TOTALES POR COLUMNA - CANTIDADES
    // =========================================================

    private Map<String, Integer>
            totalesCantidadPorCategoriaPartida =
            new LinkedHashMap<>();

    // =========================================================
    // TOTALES POR COLUMNA - VALORES
    // =========================================================

    private Map<String, BigDecimal>
            totalesValorPorCategoriaPartida =
            new LinkedHashMap<>();

    // =========================================================
    // TOTALES GENERALES
    // =========================================================

    private Integer totalCantidad;

    private BigDecimal totalValor;
}