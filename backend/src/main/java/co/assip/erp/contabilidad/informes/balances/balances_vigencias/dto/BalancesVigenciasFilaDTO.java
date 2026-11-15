package co.assip.erp.contabilidad.informes.balances.balances_vigencias.dto;

import java.math.BigDecimal;

/** Saldos del mes y movimientos hasta la fecha de corte inclusive. */
public record BalancesVigenciasFilaDTO(
        String codigoCuenta,
        String nombreCuenta,
        Integer nivelCuenta,
        String naturalezaCuenta,
        Boolean cuentaOperable,
        BigDecimal saldoAnterior,
        BigDecimal valorDebitoMes,
        BigDecimal valorCreditoMes,
        BigDecimal saldoFinal
) {}
