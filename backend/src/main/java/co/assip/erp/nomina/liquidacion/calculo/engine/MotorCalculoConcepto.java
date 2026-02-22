package co.assip.erp.nomina.liquidacion.calculo.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Motor central de cálculo de un concepto de nómina.
 * Usa la configuración de conceptos_nomina:
 * - tipo_calculo
 * - base_calculo
 * - multiplicador
 */
public final class MotorCalculoConcepto {

    private MotorCalculoConcepto() {}

    /**
     * Ejecuta el cálculo de un concepto.
     *
     * @param tipoCalculo     POR_DIAS | POR_HORAS | POR_PORCENTAJE | MANUAL | AUX_TRANSPORTE
     * @param baseCalculo     SALARIO_BASE | IBC | DEVENGADOS | NETO | NINGUNA
     * @param multiplicador   tarifa o factor del concepto
     * @param cantidad        cantidad (días, horas, unidades)
     * @param contexto        mapa de bases calculadas
     */
    public static ResultadoCalculo calcular(
            TipoCalculo tipoCalculo,
            BaseCalculo baseCalculo,
            BigDecimal multiplicador,
            BigDecimal cantidad,
            Map<BaseCalculo, BigDecimal> contexto
    ) {

        BigDecimal base = baseCalculo.resolver(contexto);
        BigDecimal valorUnitario;

        switch (tipoCalculo) {

            case POR_DIAS -> {
                // salario / 30 * multiplicador
                valorUnitario = base
                        .divide(BigDecimal.valueOf(30), 10, RoundingMode.HALF_UP)
                        .multiply(multiplicador);
            }

            case POR_HORAS -> {
                // salario / 240 * multiplicador
                valorUnitario = base
                        .divide(BigDecimal.valueOf(240), 10, RoundingMode.HALF_UP)
                        .multiply(multiplicador);
            }

            case POR_PORCENTAJE -> {
                valorUnitario = base
                        .multiply(multiplicador);
            }

            case AUX_TRANSPORTE -> {
                // se paga solo si aplica; la validación va fuera
                valorUnitario = multiplicador;
            }

            case MANUAL -> {
                // valor unitario viene ya definido
                valorUnitario = multiplicador;
            }

            default -> {
                valorUnitario = BigDecimal.ZERO;
            }
        }

        BigDecimal total = valorUnitario
                .multiply(cantidad)
                .setScale(2, RoundingMode.HALF_UP);

        return new ResultadoCalculo(
                cantidad,
                valorUnitario.setScale(2, RoundingMode.HALF_UP),
                total,
                base
        );
    }

    /**
     * Resultado estructurado del cálculo.
     */
    public record ResultadoCalculo(
            BigDecimal cantidad,
            BigDecimal valorUnitario,
            BigDecimal valorTotal,
            BigDecimal baseCalculo
    ) {}
}
