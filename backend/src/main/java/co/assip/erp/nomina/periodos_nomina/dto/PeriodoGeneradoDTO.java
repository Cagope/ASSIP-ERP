package co.assip.erp.nomina.periodos_nomina.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoGeneradoDTO {

    private Integer anio;
    private Integer mes;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private String descripcion;
}
