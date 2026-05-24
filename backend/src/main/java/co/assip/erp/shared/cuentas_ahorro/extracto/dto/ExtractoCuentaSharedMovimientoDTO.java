package co.assip.erp.shared.cuentas_ahorro.extracto.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class ExtractoCuentaSharedMovimientoDTO {

    private LocalDate fechaMovimiento;
    private LocalTime horaMovimiento;

    private String tipoMovimiento;
    private String descripcionMovimiento;

    private String tipoComprobante;
    private String numeroComprobante;

    private BigDecimal debito;
    private BigDecimal credito;

    private BigDecimal saldo;
}