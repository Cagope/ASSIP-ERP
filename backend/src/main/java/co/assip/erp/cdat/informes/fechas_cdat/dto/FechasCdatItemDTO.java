package co.assip.erp.cdat.informes.fechas_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class FechasCdatItemDTO {

    private Long idCuentaCdat;

    private String codigoCdat;

    private String documento;

    private String nombreCompleto;

    private String agencia;

    private LocalDate fechaApertura;

    private LocalDate fechaVencimiento;

    private LocalDate fechaCancelacion;

    private LocalDate fechaRenovacion;

    private Integer plazoMeses;

    private BigDecimal tasa;

    private BigDecimal valor;

    private String estado;

}