package co.assip.erp.depositos.informes.movimientos_diarios.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MovimientosDiariosResponseDTO {

    private MovimientosDiariosResumenDTO resumen;

    private List<MovimientosDiariosItemDTO> items;

}