package co.assip.erp.depositos.transferencias.dto;
import java.math.BigDecimal;
import java.util.List;
public record TransferenciaCuentasResultadoDTO(
    Integer idCuentaOrigen,
    Integer idCuentaDestino,
    BigDecimal valor,
    BigDecimal saldoOrigenFinal,
    BigDecimal saldoDestinoFinal,
    List<Comprobante> comprobantes
) {
    public record Comprobante(Integer idAgencia, String tipoComprobante,
        String numeroComprobante) {}
}
