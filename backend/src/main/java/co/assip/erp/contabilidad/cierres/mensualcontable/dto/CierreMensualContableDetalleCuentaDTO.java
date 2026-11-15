package co.assip.erp.contabilidad.cierres.mensualcontable.dto;

import java.math.BigDecimal;

/**
 * Representa la consolidación mensual de una cuenta contable.
 * Incluye acumulados anteriores, movimientos del mes y saldos finales.
 * Los valores son de consulta y no modifican la contabilidad.
 */
public record CierreMensualContableDetalleCuentaDTO(
        Integer idCatalogoCuenta,
        String codigoCuenta,
        String nombreCuenta,
        String naturalezaCuenta,
        Integer nivelCuenta,
        Boolean cuentaOperable,
        BigDecimal debitoAnterior,
        BigDecimal creditoAnterior,
        BigDecimal saldoAnterior,
        BigDecimal debitoMes,
        BigDecimal creditoMes,
        BigDecimal debitoAcumulado,
        BigDecimal creditoAcumulado,
        BigDecimal saldoFinal
) {
}