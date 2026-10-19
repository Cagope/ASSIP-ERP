
package co.assip.erp.depositos.retiro_asociado.dto;

import co.assip.erp.depositos.retiro_asociado.RetiroAsociadoRepository.CuentaRetiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RetiroAsociadoPreviewDTO(
        Integer idDatosPersonal,
        Integer idAgenciaOrigen,
        List<CuentaRetiro> cuentas,
        BigDecimal totalSaldos,
        BigDecimal totalCaja,
        BigDecimal totalCxp,
        BigDecimal diferencia,
        List<ComprobantePreview> comprobantes,
        String numeroActa,
        LocalDate fechaActa
) {
    public record ComprobantePreview(
            Integer idAgencia,
            String nombreAgencia,
            String tipoComprobante,
            String concepto,
            List<MovimientoPreview> movimientos,
            BigDecimal totalDebito,
            BigDecimal totalCredito
    ) {}

    public record MovimientoPreview(
            String codigoCuenta,
            String nombreCuenta,
            String detalle,
            BigDecimal valorDebito,
            BigDecimal valorCredito
    ) {}
}
