package co.assip.erp.cajas.provisiones.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CajasProvisionCerrarDTO {

    private Long idProvision;

    private BigDecimal efectivoFin;
    private BigDecimal chequesFin;

    private String observacion;

}