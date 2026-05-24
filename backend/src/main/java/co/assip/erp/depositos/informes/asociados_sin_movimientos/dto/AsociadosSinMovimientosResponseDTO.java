package co.assip.erp.depositos.informes.asociados_sin_movimientos.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AsociadosSinMovimientosResponseDTO {

    private AsociadosSinMovimientosResumenDTO resumen;

    private List<AsociadosSinMovimientosItemDTO> items;

}