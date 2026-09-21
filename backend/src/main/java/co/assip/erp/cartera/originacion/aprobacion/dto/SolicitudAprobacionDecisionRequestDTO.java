package co.assip.erp.cartera.originacion.aprobacion.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SolicitudAprobacionDecisionRequestDTO {

    private Integer idAprobacionDecision;

    private String concepto;

    private String numeroActa;
    private LocalDate fechaActa;
}