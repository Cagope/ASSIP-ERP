package co.assip.erp.shared.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MathUtils {

    private MathUtils() {
    }

    /**
     * Redondea a pesos (sin decimales)
     */
    public static BigDecimal pesos(BigDecimal valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        return valor.setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * División segura con redondeo financiero
     */
    public static BigDecimal dividir(BigDecimal valor, BigDecimal divisor) {

        if (valor == null || divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return valor.divide(divisor, 8, RoundingMode.HALF_UP);
    }

    /**
     * Multiplicación segura
     */
    public static BigDecimal multiplicar(BigDecimal valor, BigDecimal multiplicador) {

        if (valor == null || multiplicador == null) {
            return BigDecimal.ZERO;
        }

        return valor.multiply(multiplicador);
    }

}