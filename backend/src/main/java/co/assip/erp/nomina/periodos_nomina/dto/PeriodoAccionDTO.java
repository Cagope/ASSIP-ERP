package co.assip.erp.nomina.periodos_nomina.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoAccionDTO {

    private Integer idPeriodo;

    // ABRIR / CERRAR / LIQUIDAR / CONTABILIZAR
    private String accion;

    // opcional (si quieres dejar rastro)
    private String observacion;
}
