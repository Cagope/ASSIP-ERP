package co.assip.erp.cajas.provisiones.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CajasProvisionListDTO {

    private Long idProvision;

    private Long idCaja;
    private String codigoCaja;
    private String descripcionCaja;

    private LocalDate fechaContable;

    private String estado;

    private BigDecimal efectivoInicio;
    private BigDecimal efectivoFin;

    private BigDecimal chequesInicio;
    private BigDecimal chequesFin;

}