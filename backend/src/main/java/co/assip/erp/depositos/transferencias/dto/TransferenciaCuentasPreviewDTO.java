package co.assip.erp.depositos.transferencias.dto;
import java.math.BigDecimal;
import java.util.List;
public record TransferenciaCuentasPreviewDTO(
    TransferenciaCuentasCuentaDTO origen,
    TransferenciaCuentasCuentaDTO destino,
    BigDecimal valor,
    String concepto,
    BigDecimal saldoOrigenFinal,
    BigDecimal saldoDestinoFinal,
    List<Comprobante> comprobantes
) {
    public record Movimiento(String codigoCuenta, String nombreCuenta,
        String detalle, BigDecimal valorDebito, BigDecimal valorCredito) {}
    public record Comprobante(Integer idAgencia, String nombreAgencia,
        String tipoComprobante, String concepto,
        List<Movimiento> movimientos, BigDecimal totalDebito, BigDecimal totalCredito) {}
}
