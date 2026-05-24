package co.assip.erp.depositos.movimientos.cuentasahorro.dto;

import lombok.*;

import java.math.BigDecimal;
import co.assip.erp.sarlaft.evaluacion.dto.EvaluacionResultado;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoCuentaPreviewDTO {

    private Integer idCuentaAhorro;

    private String codigoCuenta;
    private String documento;
    private String nombreAsociado;

    private String codigoForma;
    private String nombreForma;

    private String tipoMovimiento;
    private String descripcionMovimiento;
    private String accionMovimiento;

    private LocalDate fechaMovimiento;

    private BigDecimal saldoActual;
    private BigDecimal valorMovimiento;
    private BigDecimal valorGmf;
    private BigDecimal saldoFinal;

    private Boolean contabilizacionDiaria;
    private Boolean generaGmf;

    private EvaluacionResultado sarlaft;
}