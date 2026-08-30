package co.assip.erp.cartera.analisis.cosechas.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CosechaCorteDTO {

    private LocalDate fechaCorte;

    private Integer cantidadCreditos;
}