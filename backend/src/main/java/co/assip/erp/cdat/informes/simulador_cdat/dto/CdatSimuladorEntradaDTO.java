package co.assip.erp.cdat.informes.simulador_cdat.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CdatSimuladorEntradaDTO {

    private Integer idAgencia;

    private BigDecimal valorCdat;

    private BigDecimal tasaNominalAnual;

    private LocalDate fechaApertura;

    private Integer plazoMeses;

    /**
     * MENSUAL / VENCIMIENTO
     */
    private String formaPagoInteres;

    private Boolean aplicaRetencion;
}