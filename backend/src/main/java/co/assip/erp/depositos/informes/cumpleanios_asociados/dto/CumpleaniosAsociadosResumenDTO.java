package co.assip.erp.depositos.informes.cumpleanios_asociados.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CumpleaniosAsociadosResumenDTO {

    private Integer totalAsociados;

    private String rangoFechas;

}