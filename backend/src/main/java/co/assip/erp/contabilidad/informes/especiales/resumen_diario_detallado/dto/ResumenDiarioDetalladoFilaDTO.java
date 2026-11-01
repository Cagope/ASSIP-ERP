package co.assip.erp.contabilidad.informes.especiales.resumen_diario_detallado.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa un movimiento contable individual del Resumen diario detallado.
 * Conserva la identificación del comprobante, cuenta, tercero y trazabilidad.
 */
public record ResumenDiarioDetalladoFilaDTO(
        Integer idAuxiliarContable,
        LocalDate fecha,
        String tipoComprobante,
        String numeroComprobante,
        String codigoCuenta,
        String nombreCuenta,
        String documentoTercero,
        String nombreTercero,
        String detalleMovimiento,
        BigDecimal valorDebito,
        BigDecimal valorCredito,
        String origenModulo,
        String documentoOrigen,
        String documentoSoporte
) {
}