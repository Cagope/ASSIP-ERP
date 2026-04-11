package co.assip.erp.nomina.contabilizacion.prestaciones_sociales.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestacionesSocialesContabilizacionResultadoDTO {

    private Integer idPeriodoNomina;
    private Integer cantidadAgencias;
    private List<PrestacionesSocialesComprobanteGeneradoDTO> comprobantes;
}