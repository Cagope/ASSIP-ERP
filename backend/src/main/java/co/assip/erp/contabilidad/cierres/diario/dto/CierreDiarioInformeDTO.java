
package co.assip.erp.contabilidad.cierres.diario.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Contiene la información del informe contable de una fecha
 * y agencia, para impresión y exportación a Excel.
 *
 * Los valores del informe se obtienen de los auxiliares
 * contables vigentes y no modifican el cierre diario.
 */
public record CierreDiarioInformeDTO(
        Integer idAgencia,
        String codigoAgencia,
        String nombreAgencia,
        LocalDate fecha,
        String estadoDia,
        String estadoPeriodoMensual,
        LocalDateTime fechaGeneracion,
        Long cantidadComprobantes,
        Long cantidadMovimientos,
        BigDecimal totalDebitos,
        BigDecimal totalCreditos,
        BigDecimal diferencia,
        List<CierreDiarioDetalleCuentaDTO> resumenCuentas,
        List<ResumenMovimientoDTO> movimientos
) {

    /**
     * Representa una línea individual del informe contable.
     * Conserva la identificación del comprobante, la cuenta,
     * el tercero y los valores registrados.
     */
    public record ResumenMovimientoDTO(
            Long idAuxiliarContable,
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
}
