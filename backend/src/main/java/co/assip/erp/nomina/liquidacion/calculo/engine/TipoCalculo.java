package co.assip.erp.nomina.liquidacion.calculo.engine;

/**
 * Tipos de cálculo soportados por el motor.
 * Debe corresponder a conceptos_nomina.tipo_calculo.
 */
public enum TipoCalculo {

    MANUAL,          // viene de novedades (valor directo)
    POR_DIAS,        // salario_base / 30 * dias
    POR_HORAS,       // valor_hora * horas * multiplicador
    POR_PORCENTAJE,  // base * multiplicador (ej 0.04)
    AUX_TRANSPORTE;  // regla especial (si aplica)

    public static TipoCalculo fromDb(String value) {
        if (value == null || value.isBlank()) return MANUAL;
        return TipoCalculo.valueOf(value.trim().toUpperCase());
    }
}
