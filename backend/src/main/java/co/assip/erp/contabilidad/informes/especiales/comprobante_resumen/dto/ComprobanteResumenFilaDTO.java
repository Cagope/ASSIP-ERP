package co.assip.erp.contabilidad.informes.especiales.comprobante_resumen.dto;

import java.math.BigDecimal;

/** Movimientos acumulados por cuenta contable para la agencia y período consultados. */
public record ComprobanteResumenFilaDTO(
        String codigoCuenta,
        String nombreCuenta,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos
) {}
