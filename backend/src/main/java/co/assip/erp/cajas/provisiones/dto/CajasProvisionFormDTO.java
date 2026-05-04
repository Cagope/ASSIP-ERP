package co.assip.erp.cajas.provisiones.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CajasProvisionFormDTO {

    private Long idProvision;

    private Long idCaja;

    private LocalDate fechaContable;

    private BigDecimal efectivoInicio;
    private BigDecimal chequesInicio;

    private String estado;

}