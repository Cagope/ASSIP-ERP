package co.assip.erp.shared.financiero;

import co.assip.erp.shared.financiero.dto.CuotaVariableResultado;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class CuotaMensualAnalisis {

    private static final BigDecimal MIL_DOSCIENTOS =
            new BigDecimal("1200");

    private static final MathContext MC =
            MathContext.DECIMAL128;

    private CuotaMensualAnalisis() {
    }

    /**
     * Calcula la primera cuota de una simulación mensual,
     * exclusivamente para el análisis de capacidad de pago.
     *
     * No modifica la cuota proyectada contractual.
     *
     * Condiciones de la simulación:
     *
     * - Intereses mensuales.
     * - Amortización de capital mensual.
     * - Plazo expresado en meses.
     * - Mismo tipo de cuota de la solicitud.
     *
     * Tipos de cuota ASSIP:
     *
     * 1 = Fija.
     * 2 = Variable.
     * 3 = Otra, tratada como variable según la lógica
     *     actual de SolicitudCreditoService.
     *
     * @param valorSolicitado valor del crédito.
     * @param plazoMeses plazo solicitado en meses.
     * @param tasaNominalAnual tasa nominal anual en porcentaje.
     * @param codigoTipoCuota tipo de cuota de la solicitud.
     * @return primera cuota mensual simulada.
     */
    public static BigDecimal calcularPrimeraCuota(
            BigDecimal valorSolicitado,
            Integer plazoMeses,
            BigDecimal tasaNominalAnual,
            String codigoTipoCuota
    ) {

        validarDatos(
                valorSolicitado,
                plazoMeses,
                tasaNominalAnual,
                codigoTipoCuota
        );

        String tipoCuota =
                codigoTipoCuota.trim();

        // =====================================================
        // CUOTA FIJA
        //
        // Se simula con período mensual (1 mes).
        // =====================================================

        if ("1".equals(tipoCuota)) {

            return CuotasFinancieras.cuotaFija(
                    1,
                    plazoMeses,
                    tasaNominalAnual,
                    valorSolicitado
            );
        }

        // =====================================================
        // CUOTA VARIABLE
        //
        // Primera cuota mensual:
        //
        // Primera amortización de capital
        // + interés del primer mes sobre saldo inicial.
        // =====================================================

        CuotaVariableResultado resultadoCapital =
                CuotasFinancieras.cuotaVariable(
                        valorSolicitado,
                        plazoMeses
                );

        BigDecimal primerAbonoCapital =
                resultadoCapital.valorPrimeraCuota();

        if (primerAbonoCapital == null) {

            throw new IllegalStateException(
                    "No fue posible calcular el primer "
                            + "abono mensual a capital."
            );
        }

        // =====================================================
        // INTERÉS DEL PRIMER MES
        //
        // Saldo inicial × tasa nominal anual / 1200
        // =====================================================

        BigDecimal interesPrimerMes =
                valorSolicitado
                        .multiply(
                                tasaNominalAnual,
                                MC
                        )
                        .divide(
                                MIL_DOSCIENTOS,
                                2,
                                RoundingMode.HALF_UP
                        );

        // =====================================================
        // PRIMERA CUOTA MENSUAL SIMULADA
        // =====================================================

        return primerAbonoCapital
                .add(interesPrimerMes)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private static void validarDatos(
            BigDecimal valorSolicitado,
            Integer plazoMeses,
            BigDecimal tasaNominalAnual,
            String codigoTipoCuota
    ) {

        if (valorSolicitado == null
                || valorSolicitado.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El valor solicitado debe ser mayor que cero."
            );
        }

        if (plazoMeses == null
                || plazoMeses <= 0) {

            throw new IllegalArgumentException(
                    "El plazo solicitado debe ser mayor que cero."
            );
        }

        if (tasaNominalAnual == null
                || tasaNominalAnual.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "La tasa nominal anual no es válida."
            );
        }

        if (codigoTipoCuota == null
                || codigoTipoCuota.isBlank()) {

            throw new IllegalArgumentException(
                    "El tipo de cuota es obligatorio."
            );
        }

        String tipoCuota =
                codigoTipoCuota.trim();

        if (!"1".equals(tipoCuota)
                && !"2".equals(tipoCuota)
                && !"3".equals(tipoCuota)) {

            throw new IllegalArgumentException(
                    "Tipo de cuota no válido: "
                            + codigoTipoCuota
            );
        }
    }
}