package co.assip.erp.depositos.transferencias.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
public record TransferenciaCuentasRequestDTO(
    Integer idCuentaOrigen,
    Integer idCuentaDestino,
    BigDecimal valor,
    LocalDate fechaProceso,
    String concepto
) {}
