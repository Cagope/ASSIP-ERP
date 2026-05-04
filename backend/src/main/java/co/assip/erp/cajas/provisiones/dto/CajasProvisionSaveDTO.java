package co.assip.erp.cajas.provisiones.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class CajasProvisionSaveDTO {

    private Long idProvision;

    private Long idCaja;

    private LocalDate fechaContable;

    private BigDecimal efectivoInicio;
    private BigDecimal chequesInicio;

}