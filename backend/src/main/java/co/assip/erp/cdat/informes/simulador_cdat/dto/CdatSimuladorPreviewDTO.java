package co.assip.erp.cdat.informes.simulador_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CdatSimuladorPreviewDTO {

    private BigDecimal capital;

    private BigDecimal tasaNominalAnual;

    private BigDecimal baseRetencion;

    private BigDecimal porcentajeRetencion;

    private LocalDate fechaApertura;

    private LocalDate fechaVencimiento;

    private Integer plazoMeses;

    private String formaPagoInteres;

    private BigDecimal totalInteresBruto;

    private BigDecimal totalRetencion;

    private BigDecimal totalInteresNeto;

    private BigDecimal totalPagoCliente;

    private BigDecimal valorAlVencimiento;

    private List<CdatSimuladorItemDTO> items;
}