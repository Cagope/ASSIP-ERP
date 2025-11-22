package co.assip.erp.depositos.interesdiario_sm.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 🧾 Resultado por cuenta del Interés Diario SM
 * ------------------------------------------------------------
 * Representa un registro del cálculo técnico del proceso.
 * Todos los valores monetarios y tasas se manejan con BigDecimal
 * para evitar errores por decimales.
 */
@Getter
@Setter
public class InteresDiarioSMItemDTO {

    // 🔹 Identificación de la cuenta
    private Integer idCuentaAhorro;
    private String codigoCuenta;

    // 🔹 Datos del titular
    private String documento;
    private String nombreCompleto;

    // 🔹 Cálculos financieros
    private BigDecimal saldoMinimoDia;
    private BigDecimal interesBruto;
    private BigDecimal retencion;
    private BigDecimal interesNeto;

    // 🔹 Parámetros de la forma
    private BigDecimal tasaInteres;
    private Integer tiempoLiquidacion;
    private BigDecimal minimoForma;

    // 🔹 Reglas especiales
    private Boolean aplicaRetencion;
}
