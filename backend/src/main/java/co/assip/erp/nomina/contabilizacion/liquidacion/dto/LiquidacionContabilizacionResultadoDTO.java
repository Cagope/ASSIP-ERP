package co.assip.erp.nomina.contabilizacion.liquidacion.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiquidacionContabilizacionResultadoDTO {

    private Integer idPeriodoNomina;
    private Integer cantidadAgencias;
    private List<LiquidacionComprobanteGeneradoDTO> comprobantes;
}