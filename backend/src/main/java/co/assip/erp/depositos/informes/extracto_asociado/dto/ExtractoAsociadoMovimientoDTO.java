package co.assip.erp.depositos.informes.extracto_asociado.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class ExtractoAsociadoMovimientoDTO {

    private Integer idCuentaAhorro;

    private String codigoCuenta;

    private String codigoForma;
    private String nombreForma;

    private String nombreAgencia;

    private LocalDate fechaMovimiento;
    private LocalTime horaMovimiento;

    private String tipoMovimiento;
    private String descripcionMovimiento;

    private String tipoComprobante;
    private String numeroComprobante;

    private BigDecimal debito;
    private BigDecimal credito;

    private BigDecimal saldoCuenta;

}