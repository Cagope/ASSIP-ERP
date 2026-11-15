package co.assip.erp.contabilidad.cierres.diario.dto;

import java.math.BigDecimal;

/**
 * Representa los movimientos contables agrupados por cuenta
 * para una agencia y fecha, considerando todos los tipos
 * de comprobantes con estado A.
 */
public record CierreDiarioDetalleCuentaDTO(
        Integer idCatalogoCuenta,
        String codigoCuenta,
        String nombreCuenta,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos,
        Long cantidadMovimientos
) {
}