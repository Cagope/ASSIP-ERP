package co.assip.erp.depositos.informes.extracto_cuenta.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class ExtractoCuentaMovimientoDTO {

    private LocalDate fechaMovimiento;

    private LocalTime horaMovimiento;

    // CONCEPTO
    private String tipoMovimiento;
    private String descripcionMovimiento;

    // COMPROBANTE
    private String tipoComprobante;
    private String numeroComprobante;

    // VALORES
    private BigDecimal debito;
    private BigDecimal credito;

    private BigDecimal saldo;

}