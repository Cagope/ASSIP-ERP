package co.assip.erp.depositos.informes.extracto_por_valor.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExtractoPorValorResponseDTO {

    private ExtractoPorValorResumenDTO resumen;

    private List<ExtractoPorValorItemDTO> items;

}