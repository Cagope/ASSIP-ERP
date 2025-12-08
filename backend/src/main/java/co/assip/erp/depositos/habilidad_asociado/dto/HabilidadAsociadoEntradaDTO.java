package co.assip.erp.depositos.habilidad_asociado.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 🎯 HabilidadAsociadoEntradaDTO
 * ----------------------------------------------------
 * Datos enviados desde el frontend para evaluar la
 * habilidad o inhabilidad de los asociados según
 * consignaciones de aportes sociales.
 */
@Data
public class HabilidadAsociadoEntradaDTO {

    /**
     * Id de la agencia.
     * Si es 0 o null → aplica para todas.
     */
    private Integer agenciaId;

    /**
     * Fecha inicial del rango a evaluar.
     */
    private LocalDate fechaInicio;

    /**
     * Fecha final del rango a evaluar.
     */
    private LocalDate fechaFin;

    /**
     * Valor mínimo exigido para personas menores de edad.
     */
    private BigDecimal valorMenores;

    /**
     * Valor mínimo exigido para personas mayores de edad.
     */
    private BigDecimal valorMayores;

    /**
     * Valor mínimo exigido para personas jurídicas.
     */
    private BigDecimal valorJuridicas;
}
