package co.assip.erp.depositos.informes.resumen_tipo_movimiento.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResumenTipoMovimientoResponseDTO {

    private ResumenTipoMovimientoResumenDTO resumen;

    private List<ResumenTipoMovimientoItemDTO> items;

}