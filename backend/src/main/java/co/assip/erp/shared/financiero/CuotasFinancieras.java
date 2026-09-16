package co.assip.erp.shared.financiero;

import co.assip.erp.shared.financiero.dto.CuotaVariableResultado;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class CuotasFinancieras {

    private static final BigDecimal CIEN =
            new BigDecimal("100");

    private static final BigDecimal CINCUENTA =
            new BigDecimal("50");

    private static final BigDecimal MEDIO =
            new BigDecimal("0.5");

    private static final BigDecimal TREINTA =
            new BigDecimal("30");

    private static final BigDecimal TRESCIENTOS_SESENTA =
            new BigDecimal("360");

    private static final MathContext MC =
            MathContext.DECIMAL128;

    private CuotasFinancieras() {
    }

    /**
     * Calcula una cuota fija utilizando la fórmula histórica ASSIP.
     *
     * VB:
     *
     * INTERE =
     *     ((TIEMPO * 30) / 360)
     *     * (TASA / 100)
     *
     * FACTOR =
     *     (1 + INTERE) ^ (PLAZO / TIEMPO)
     *
     * NUMERADOR =
     *     INTERE * FACTOR
     *
     * DENOMINADOR =
     *     FACTOR - 1
     *
     * CUOTA =
     *     (NUMERADOR / DENOMINADOR) * VALOR
     *
     * RETURN ROUND(CUOTA + 0.5, 0)
     *
     * @param tiempo período de pago de intereses en meses.
     * @param plazo  plazo total del crédito en meses.
     * @param tasa   tasa nominal anual expresada en porcentaje.
     * @param valor  valor del crédito.
     * @return valor de la cuota fija en pesos.
     */
    public static BigDecimal cuotaFija(
            Integer tiempo,
            Integer plazo,
            BigDecimal tasa,
            BigDecimal valor
    ) {

        validarCuotaFija(
                tiempo,
                plazo,
                tasa,
                valor
        );

        // =====================================================
        // TASA CERO
        // =====================================================

        if (tasa.compareTo(BigDecimal.ZERO) == 0) {

            BigDecimal numeroCuotas =
                    BigDecimal.valueOf(plazo)
                            .divide(
                                    BigDecimal.valueOf(tiempo),
                                    MC
                            );

            BigDecimal cuota =
                    valor.divide(
                            numeroCuotas,
                            MC
                    );

            return cuota
                    .add(MEDIO)
                    .setScale(
                            0,
                            RoundingMode.HALF_UP
                    );
        }

        // =====================================================
        // INTERÉS DEL PERÍODO
        //
        // VB:
        // INTERE =
        // ((TIEMPO * 30) / 360) * (TASA / 100)
        // =====================================================

        BigDecimal interes =
                BigDecimal.valueOf(tiempo)
                        .multiply(TREINTA)
                        .divide(
                                TRESCIENTOS_SESENTA,
                                MC
                        )
                        .multiply(
                                tasa.divide(CIEN, MC)
                        );

        // =====================================================
        // NÚMERO DE PERÍODOS
        //
        // PLAZO / TIEMPO
        // =====================================================

        BigDecimal numeroPeriodos =
                BigDecimal.valueOf(plazo)
                        .divide(
                                BigDecimal.valueOf(tiempo),
                                MC
                        );

        // =====================================================
        // FACTOR
        //
        // (1 + INTERE) ^ (PLAZO / TIEMPO)
        //
        // Se utiliza StrictMath porque el exponente puede
        // no ser entero, igual que en la rutina histórica.
        // =====================================================

        double factorDouble =
                StrictMath.pow(
                        BigDecimal.ONE
                                .add(interes)
                                .doubleValue(),
                        numeroPeriodos.doubleValue()
                );

        BigDecimal factor =
                BigDecimal.valueOf(factorDouble);

        // =====================================================
        // NUMERADOR
        // =====================================================

        BigDecimal numerador =
                interes.multiply(
                        factor,
                        MC
                );

        // =====================================================
        // DENOMINADOR
        // =====================================================

        BigDecimal denominador =
                factor.subtract(
                        BigDecimal.ONE,
                        MC
                );

        if (denominador.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException(
                    "No fue posible calcular la cuota fija: denominador igual a cero"
            );
        }

        // =====================================================
        // CUOTA
        // =====================================================

        BigDecimal cuota =
                numerador
                        .divide(
                                denominador,
                                MC
                        )
                        .multiply(
                                valor,
                                MC
                        );

        // =====================================================
        // REDONDEO HISTÓRICO
        //
        // ROUND(CUOTA + 0.5, 0)
        // =====================================================

        return cuota
                .add(MEDIO)
                .setScale(
                        0,
                        RoundingMode.HALF_UP
                );
    }

    /**
     * Calcula la distribución de capital para una cuota variable.
     *
     * Equivalente a la rutina histórica fn_cuota_variable.
     *
     * Esta función NO calcula intereses.
     * Únicamente distribuye el capital.
     *
     * @param valorCredito valor total del crédito.
     * @param numeroCuotas número de cuotas de capital.
     * @return cuota regular y primera cuota de ajuste.
     */
    public static CuotaVariableResultado cuotaVariable(
            BigDecimal valorCredito,
            Integer numeroCuotas
    ) {

        if (valorCredito == null
                || valorCredito.compareTo(BigDecimal.ZERO) <= 0
                || numeroCuotas == null
                || numeroCuotas <= 0) {

            return new CuotaVariableResultado(
                    null,
                    null
            );
        }

        // =====================================================
        // CUOTA INICIAL
        //
        // v_cuotas := p_valor_credito / p_numero_cuotas
        // =====================================================

        BigDecimal cuotas =
                valorCredito.divide(
                        BigDecimal.valueOf(numeroCuotas),
                        MC
                );

        // =====================================================
        // RESIDUO CONTRA 100
        //
        // v_cuotast := MOD(v_cuotas, 100)
        // =====================================================

        BigDecimal cuotaResto =
                cuotas.remainder(CIEN);

        BigDecimal cuotaPrimera;

        // =====================================================
        // AJUSTE HISTÓRICO A CENTENAS
        // =====================================================

        if (cuotaResto.compareTo(BigDecimal.ZERO) != 0) {

            cuotas =
                    cuotas.subtract(CINCUENTA);

            BigDecimal cuotaRedondeada =
                    cuotas
                            .divide(
                                    CIEN,
                                    MC
                            )
                            .setScale(
                                    0,
                                    RoundingMode.HALF_UP
                            );

            // Equivalente a:
            // TRUNC(ROUND(v_cuotas / 100, 0))
            cuotaRedondeada =
                    cuotaRedondeada.setScale(
                            0,
                            RoundingMode.DOWN
                    );

            cuotas =
                    cuotaRedondeada.multiply(CIEN);

            BigDecimal totalCuotas =
                    cuotas.multiply(
                            BigDecimal.valueOf(
                                    numeroCuotas - 1L
                            )
                    );

            cuotaPrimera =
                    valorCredito.subtract(
                            totalCuotas
                    );

        } else {

            BigDecimal totalCuotas =
                    cuotas.multiply(
                            BigDecimal.valueOf(
                                    numeroCuotas - 1L
                            )
                    );

            cuotaPrimera =
                    valorCredito.subtract(
                            totalCuotas
                    );
        }

        // =====================================================
        // REGLA HISTÓRICA
        //
        // IF v_cuota1 = p_valor_credito THEN
        //     v_cuotas := 0;
        // END IF;
        // =====================================================

        if (cuotaPrimera.compareTo(valorCredito) == 0) {
            cuotas = BigDecimal.ZERO;
        }

        return new CuotaVariableResultado(
                cuotas,
                cuotaPrimera
        );
    }

    private static void validarCuotaFija(
            Integer tiempo,
            Integer plazo,
            BigDecimal tasa,
            BigDecimal valor
    ) {

        if (tiempo == null || tiempo <= 0) {
            throw new IllegalArgumentException(
                    "El tiempo debe ser mayor que cero"
            );
        }

        if (plazo == null || plazo <= 0) {
            throw new IllegalArgumentException(
                    "El plazo debe ser mayor que cero"
            );
        }

        if (tasa == null
                || tasa.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "La tasa no puede ser negativa"
            );
        }

        if (valor == null
                || valor.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El valor debe ser mayor que cero"
            );
        }
    }
}