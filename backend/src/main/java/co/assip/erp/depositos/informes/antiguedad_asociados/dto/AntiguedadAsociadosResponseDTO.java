package co.assip.erp.depositos.informes.antiguedad_asociados.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AntiguedadAsociadosResponseDTO {

    private List<AntiguedadAsociadosResumenDTO> resumen;
    private List<AntiguedadAsociadosItemDTO> mayoresAntiguedad;
    private List<AntiguedadAsociadosItemDTO> itemsExcel;

}