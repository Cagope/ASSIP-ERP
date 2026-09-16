package co.assip.erp.cdat.analisis.tasascondiciones.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AnalisisTasasCdatCorteDTO {

    private LocalDate fechaCorte;

    private Integer anio;

    private Integer mes;

    private Integer cantidadAgencias;

    private Integer cantidadCdats;
}