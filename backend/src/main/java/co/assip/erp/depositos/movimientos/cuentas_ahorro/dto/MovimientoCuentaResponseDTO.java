package co.assip.erp.depositos.movimientos.cuentasahorro.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoCuentaResponseDTO {

    private Integer idCuentaAhorro;

    private String codigoCuenta;
    private String documento;
    private String nombreAsociado;

    private String tipoMovimiento;
    private String descripcionMovimiento;

    private LocalDate fechaMovimiento;

    private BigDecimal valorMovimiento;
    private BigDecimal valorGmf;

    private BigDecimal saldoAnterior;
    private BigDecimal saldoFinal;

    private String tipoComprobante;
    private String numeroComprobante;

    private String mensaje;
}