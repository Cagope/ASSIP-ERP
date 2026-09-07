package co.assip.erp.depositos.analisis.concentracion.dto;

import lombok.Data;

@Data
public class ConcentracionDepositosRequestDTO {

    /**
     * Fecha obligatoria del análisis.
     * Formato: yyyy-MM-dd
     */
    private String fechaCorte;

    /**
     * 0 = todas las agencias.
     */
    private Integer idAgencia;

    /**
     * 0 = todas las formas de ahorro.
     *
     * Cuando es 0:
     * - la concentración se calcula sobre captaciones;
     * - los aportes sociales se informan separadamente.
     *
     * Cuando se selecciona una forma específica:
     * - se analiza únicamente esa forma,
     *   incluyendo aportes sociales si esa es la seleccionada.
     */
    private Integer idFormaAhorro;
}