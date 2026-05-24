package co.assip.erp.depositos.informes.saldos_rangos_edad.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SaldosRangosEdadResponseDTO {

    private List<SaldosRangosEdadResumenDTO> resumen;
    private List<SaldosRangosEdadItemDTO> itemsExcel;

}