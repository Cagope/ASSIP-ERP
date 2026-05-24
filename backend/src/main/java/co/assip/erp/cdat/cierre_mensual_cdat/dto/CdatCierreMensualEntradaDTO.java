package co.assip.erp.cdat.cierre_mensual_cdat.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CdatCierreMensualEntradaDTO {

    private Integer idAgencia;

    private LocalDate fechaCorte;

    private Boolean confirmado;
}