package co.assip.erp.nomina.periodos_nomina.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodosGenerarRequestDTO {

    private Integer idAgencia;

    private Integer anio;

    // MENSUAL / QUINCENAL / SEMANAL (preparado a futuro)
    private String tipoPeriodo;
}
