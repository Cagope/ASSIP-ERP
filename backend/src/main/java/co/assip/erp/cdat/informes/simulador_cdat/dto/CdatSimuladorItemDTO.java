package co.assip.erp.cdat.informes.simulador_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CdatSimuladorItemDTO {

    private Integer periodo;

    private LocalDate fechaPago;

    private Integer dias;

    private BigDecimal capital;

    private BigDecimal interesBruto;

    private BigDecimal baseRetencion;

    private BigDecimal porcentajeRetencion;

    private BigDecimal valorRetencion;

    private BigDecimal interesNeto;

    private BigDecimal pagoCliente;

    private BigDecimal saldoFinal;
}