package co.assip.erp.depositos.informes.saldos_menores.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SaldosMenoresResponseDTO {

    private SaldosMenoresResumenDTO resumen;
    private List<SaldosMenoresItemDTO> items;

}