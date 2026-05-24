package co.assip.erp.depositos.informes.intereses_retencion.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InteresesRetencionResponseDTO {

    private InteresesRetencionResumenDTO resumen;

    private List<InteresesRetencionItemDTO> items;

}