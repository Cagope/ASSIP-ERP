package co.assip.erp.cdat.informes.fechas_cdat.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FechasCdatResponseDTO {

    private FechasCdatResumenDTO resumen;
    private List<FechasCdatItemDTO> resultados;

}