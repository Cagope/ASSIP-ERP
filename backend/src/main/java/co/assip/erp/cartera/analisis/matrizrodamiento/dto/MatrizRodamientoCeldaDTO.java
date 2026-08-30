package co.assip.erp.cartera.analisis.matrizrodamiento.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MatrizRodamientoCeldaDTO {

    // =========================================================
    // TRANSICIÓN
    // =========================================================

    /**
     * Categoría del período anterior.
     *
     * Corresponde a la FILA de la matriz.
     */
    private String categoriaAnterior;

    /**
     * Categoría de los datos de partida.
     *
     * Corresponde a la COLUMNA de la matriz.
     */
    private String categoriaPartida;

    // =========================================================
    // MATRIZ POR CANTIDADES
    // =========================================================

    private Integer cantidad;

    private BigDecimal porcentajeCantidad;

    // =========================================================
    // MATRIZ POR VALORES
    // =========================================================

    /**
     * El valor corresponde al saldo de los datos de partida.
     *
     * Es equivalente a SALDOI_T en el VB.
     */
    private BigDecimal valor;

    private BigDecimal porcentajeValor;
}