package co.assip.erp.nomina.liquidacion.calculo.engine;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Define la base de cálculo para un concepto.
 * Debe corresponder a conceptos_nomina.base_calculo.
 */
public enum BaseCalculo {

    SALARIO_BASE,
    IBC,
    DEVENGADOS,
    NETO,
    NINGUNA;

    /**
     * Obtiene el valor de la base desde el contexto de liquidación.
     */
    public BigDecimal resolver(Map<BaseCalculo, BigDecimal> contexto) {
        return contexto.getOrDefault(this, BigDecimal.ZERO);
    }

    public static BaseCalculo fromDb(String value) {
        if (value == null || value.isBlank()) return NINGUNA;
        return BaseCalculo.valueOf(value.trim().toUpperCase());
    }
}
