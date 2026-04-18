package co.assip.erp.nomina.vacaciones.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacacionPreviewRequestDTO {

    private Integer idContrato;
    private LocalDate fechaLiquidacion;
}