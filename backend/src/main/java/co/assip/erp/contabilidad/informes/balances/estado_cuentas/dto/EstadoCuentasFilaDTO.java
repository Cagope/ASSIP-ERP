package co.assip.erp.contabilidad.informes.balances.estado_cuentas.dto;

import java.math.BigDecimal;

/** Saldos del mes y movimientos hasta la fecha de corte inclusive. */
public record EstadoCuentasFilaDTO(
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
