package co.assip.erp.shared.financiero;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class TasasFinancieras {

    private static final BigDecimal CIEN =
            new BigDecimal("100");

    private static final MathContext MC =
            MathContext.DECIMAL128;

    private TasasFinancieras() {
    }

    /**
     * Calcula la tasa efectiva anual a partir de una tasa nominal anual
     * y del período de liquidación expresado en meses.
     *
     * Equivalente histórico:
     *
     * INTERES1 = (EJE / 100) / Int(12 / TIEMPO)
     * CUOTA1   = ((INTERES1 + 1) ^ Int(12 / TIEMPO))
     * CUOTA2   = (CUOTA1 - 1) * 100
     *
     * Regla ASSIP:
     * períodos superiores a 12 meses se calculan como 12 meses.
     *
     * @param tasaNominalAnual tasa nominal anual expresada en porcentaje.
     *                         Ejemplo: 18 representa 18%.
     * @param periodoMeses     período de liquidación en meses.
     * @return tasa efectiva anual expresada en porcentaje,
     *         redondeada a 6 decimales.
     */
    public static BigDecimal tasaEfectivaAnual(
            BigDecimal tasaNominalAnual,
            Integer periodoMeses
    ) {

        if (tasaNominalAnual == null) {
            return null;
        }

        if (periodoMeses == null || periodoMeses <= 0) {
            return null;
        }

        if (tasaNominalAnual.compareTo(BigDecimal.ZERO) < 0) {
            return null;
        }

        if (tasaNominalAnual.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
        }

        // =====================================================
        // NORMALIZACIÓN DEL PERÍODO
        // =====================================================

        int periodoNormalizado =
                Math.min(periodoMeses, 12);

        // =====================================================
        // NÚMERO DE PERÍODOS EN EL AÑO
        //
        // VB:
        // Int(12 / TIEMPO)
        // =====================================================

        int periodosAnio =
                12 / periodoNormalizado;

        if (periodosAnio <= 0) {
            return null;
        }

        // =====================================================
        // TASA PERIÓDICA
        //
        // VB:
        // INTERES1 = (EJE / 100) / Int(12 / TIEMPO)
        // =====================================================

        BigDecimal tasaPeriodica =
                tasaNominalAnual
                        .divide(CIEN, MC)
                        .divide(
                                BigDecimal.valueOf(periodosAnio),
                                MC
                        );

        // =====================================================
        // TASA EFECTIVA ANUAL
        //
        // VB:
        // CUOTA1 = ((INTERES1 + 1) ^ Int(12 / TIEMPO))
        // CUOTA2 = (CUOTA1 - 1) * 100
        // =====================================================

        BigDecimal factor =
                BigDecimal.ONE
                        .add(tasaPeriodica)
                        .pow(periodosAnio, MC);

        BigDecimal tasaEfectiva =
                factor
                        .subtract(BigDecimal.ONE)
                        .multiply(CIEN);

        return tasaEfectiva.setScale(
                6,
                RoundingMode.HALF_UP
        );
    }
}