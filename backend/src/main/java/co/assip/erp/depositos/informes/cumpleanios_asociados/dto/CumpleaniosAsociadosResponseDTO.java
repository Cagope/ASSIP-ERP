package co.assip.erp.depositos.informes.cumpleanios_asociados.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CumpleaniosAsociadosResponseDTO {

    private CumpleaniosAsociadosResumenDTO resumen;

    private List<CumpleaniosAsociadosItemDTO> items;

}