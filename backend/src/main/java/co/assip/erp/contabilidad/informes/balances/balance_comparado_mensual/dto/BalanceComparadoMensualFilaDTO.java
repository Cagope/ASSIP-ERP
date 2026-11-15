package co.assip.erp.contabilidad.informes.balances.balance_comparado_mensual.dto;

import java.math.BigDecimal;

/**
 * Representa una cuenta contable comparada entre dos períodos seleccionados.
 * Los saldos y variaciones se conservan como números para pantalla, PDF y Excel.
 */
public record BalanceComparadoMensualFilaDTO(
        String codigoCuenta,
        String nombreCuenta,
        Integer nivelCuenta,
        String naturalezaCuenta,
        Boolean cuentaOperable,
        BigDecimal saldoPeriodo1,
        BigDecimal saldoPeriodo2,
        BigDecimal variacionValor,
        BigDecimal variacionPorcentaje
) {
}
