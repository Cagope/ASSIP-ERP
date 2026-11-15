package co.assip.erp.contabilidad.informes.balances.balance_detallado_mensual.dto;

import java.math.BigDecimal;

/**
 * Representa una cuenta en el balance mensual, con su movimiento y saldo.
 * Los importes son numéricos; el formato colombiano se aplica al presentar o exportar.
 * En modo comparativo, los tres últimos valores permiten evaluar el mismo mes del año anterior.
 */
public record BalanceDetalladoMensualFilaDTO(
        String codigoCuenta,
        String nombreCuenta,
        Integer nivelCuenta,
        String naturalezaCuenta,
        Boolean cuentaOperable,
        BigDecimal saldoAnterior,
        BigDecimal valorDebitoMes,
        BigDecimal valorCreditoMes,
        BigDecimal saldoFinal,
        BigDecimal saldoMismoMesAnoAnterior,
        BigDecimal variacionValor,
        BigDecimal variacionPorcentaje
) {
}