package co.assip.erp.depositos.informes.estadisticos_asociados.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EstadisticosAsociadosResponseDTO {

    private EstadisticosAsociadosResumenDTO resumen;

    private List<EstadisticosAsociadosItemDTO> items;

    private List<EstadisticosAsociadosDetalleDTO> detalle;

}