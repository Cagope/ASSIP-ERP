package co.assip.erp.depositos.informes.gmf_semanal.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class GmfSemanalResponseDTO {

    private LocalDate fechaInicial;

    private LocalDate fechaFinal;

    private Integer idAgencia;

    private Integer numeroSemana;

    private String codigoForma;

    private GmfSemanalResumenDTO resumen;

    private List<GmfSemanalItemDTO> items;

}