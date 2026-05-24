package co.assip.erp.depositos.movimientos.cuentasahorro.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoCuentaRequestDTO {

    private Integer idCuentaAhorro;

    private LocalDate fechaMovimiento;

    private String tipoMovimiento;

    private BigDecimal valorMovimiento;

    private String tipoComprobante;
    private String numeroComprobante;

    private String detalle;

    private Integer idUsuario;

    private Integer idAgencia;
}