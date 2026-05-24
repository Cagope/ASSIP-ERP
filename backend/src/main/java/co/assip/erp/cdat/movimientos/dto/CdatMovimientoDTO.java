package co.assip.erp.cdat.movimientos.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CdatMovimientoDTO {

    private Long idCuentaCdat;

    private LocalDate fechaMovimiento;
    private LocalTime horaMovimiento;

    private String tipoMovimiento;

    private BigDecimal valorDebito;
    private BigDecimal valorCredito;

    private String tipoComprobante;
    private String numeroComprobante;

    private String modulo;
    private String tarjeta;
    private String establecimiento;

    private String moduloOrigen;
    private String procesoOrigen;
    private Long idOrigen;

    private String documentoSoporte;

    private Integer idUsuario;

}