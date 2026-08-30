package co.assip.erp.cartera.analisis.matrizrodamiento.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MatrizRodamientoCorteDTO {

    private LocalDate fechaCorte;

    private Integer cantidadCreditos;
}