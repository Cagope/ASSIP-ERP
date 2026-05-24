package co.assip.erp.depositos.informes.movimientos_diarios.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class MovimientosDiariosItemDTO {

    private Long idExtractoCuentaAhorro;

    private LocalDate fechaMovimiento;
    private LocalTime horaMovimiento;

    private String codigoAgencia;
    private String nombreAgencia;

    private String codigoCuenta;

    private String documento;
    private String nombreCompleto;

    private String codigoForma;
    private String nombreForma;

    private String codigoMovimiento;
    private String nombreMovimiento;

    private BigDecimal debito;
    private BigDecimal credito;

}