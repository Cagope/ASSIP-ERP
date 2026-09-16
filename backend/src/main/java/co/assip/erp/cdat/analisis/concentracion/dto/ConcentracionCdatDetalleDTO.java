package co.assip.erp.cdat.analisis.concentracion.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ConcentracionCdatDetalleDTO {

    private Long idCuentaCdat;
    private String codigoCdat;

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    private LocalDate fechaApertura;
    private LocalDate fechaVencimiento;

    private Integer plazoMeses;
    private Integer plazoDias;

    private BigDecimal tasaNominalAnual;

    private BigDecimal valorApertura;
    private BigDecimal saldoActual;

    private BigDecimal participacionDepositante;
}