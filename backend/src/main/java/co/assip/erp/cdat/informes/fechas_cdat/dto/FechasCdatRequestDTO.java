package co.assip.erp.cdat.informes.fechas_cdat.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FechasCdatRequestDTO {

    private String tipoInforme;

    private LocalDate fechaInicial;

    private LocalDate fechaFinal;

}