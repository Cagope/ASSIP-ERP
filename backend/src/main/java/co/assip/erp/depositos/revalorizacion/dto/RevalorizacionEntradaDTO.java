package co.assip.erp.depositos.revalorizacion.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 🎯 RevalorizacionEntradaDTO
 * ----------------------------------------------------
 * Datos de entrada enviados desde el frontend para el
 * proceso técnico de revalorización de cuentas de ahorro.
 */
@Data
public class RevalorizacionEntradaDTO {

    /**
     * Id de la agencia (general.datos_agencias.id_agencia)
     */
    private Integer agenciaId;

    /**
     * Fecha inicial del período a evaluar.
     */
    private LocalDate fechaInicio;

    /**
     * Fecha final del período a evaluar.
     */
    private LocalDate fechaFin;

    /**
     * Fecha contable para aplicar movimientos.
     * También sirve para obtener el saldo al corte.
     */
    private LocalDate fechaContabilizacion;

    /**
     * Tasa de revalorización (%).
     * Ej: 5.2 representa 5.2%
     */
    private BigDecimal tasaRevalorizacion;
}
