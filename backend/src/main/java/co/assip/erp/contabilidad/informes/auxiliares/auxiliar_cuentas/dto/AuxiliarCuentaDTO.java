
package co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado financiero de una cuenta auxiliar operable.
 * Contiene saldo inicial, movimientos, totales y saldo final.
 */
public record AuxiliarCuentaDTO(
        Integer idCatalogoCuenta,
        String codigoCuenta,
        String nombreCuenta,
        String naturalezaCuenta,
        BigDecimal saldoInicial,
        List<AuxiliarCuentaMovimientoDTO> movimientos,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos,
        BigDecimal saldoFinal
) {
}
