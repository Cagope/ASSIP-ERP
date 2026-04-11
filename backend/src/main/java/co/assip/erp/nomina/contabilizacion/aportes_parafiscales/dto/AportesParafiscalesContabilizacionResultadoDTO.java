package co.assip.erp.nomina.contabilizacion.aportes_parafiscales.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AportesParafiscalesContabilizacionResultadoDTO {

    private Integer idPeriodoNomina;
    private Integer cantidadAgencias;
    private List<AportesParafiscalesComprobanteGeneradoDTO> comprobantes;
}