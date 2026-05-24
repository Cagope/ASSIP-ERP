package co.assip.erp.depositos.informes.resumen_tipo_movimiento.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class ResumenTipoMovimientoItemDTO {

    private String codigoMovimiento;
    private String nombreMovimiento;

    private Integer cantidadMovimientos;

    private BigDecimal totalDebitos;
    private BigDecimal totalCreditos;
    private BigDecimal neto;

    // Detalle segundo nivel
    private Long idExtractoCuentaAhorro;

    private LocalDate fechaMovimiento;
    private LocalTime horaMovimiento;

    private String codigoAgencia;
    private String nombreAgencia;

    private String codigoForma;
    private String nombreForma;

    private String codigoCuenta;
    private String documento;
    private String nombreCompleto;

    private BigDecimal debito;
    private BigDecimal credito;

}