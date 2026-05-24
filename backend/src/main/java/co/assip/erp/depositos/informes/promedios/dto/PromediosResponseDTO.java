package co.assip.erp.depositos.informes.promedios.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PromediosResponseDTO {

    private PromediosResumenDTO resumen;

    private List<PromediosFormaDTO> formas;

    private List<PromediosItemDTO> mayores;

    private List<PromediosItemDTO> menores;

    private List<PromediosItemDTO> itemsExcel;

}