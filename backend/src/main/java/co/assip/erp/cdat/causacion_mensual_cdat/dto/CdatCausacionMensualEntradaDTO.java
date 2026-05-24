package co.assip.erp.cdat.causacion_mensual_cdat.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CdatCausacionMensualEntradaDTO {

    private Integer idAgencia;
    private LocalDate fechaCorte;
    private LocalDate fechaContable;

    private String tipoComprobante;
    private String numeroComprobante;

    private Boolean confirmado;
}