package co.assip.erp.depositos.informes.entradas_salidas.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EntradasSalidasResponseDTO {

    private EntradasSalidasResumenDTO resumen;

    private List<EntradasSalidasItemDTO> items;

}