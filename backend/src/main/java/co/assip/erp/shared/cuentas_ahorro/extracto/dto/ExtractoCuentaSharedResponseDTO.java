package co.assip.erp.shared.cuentas_ahorro.extracto.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExtractoCuentaSharedResponseDTO {

    private ExtractoCuentaSharedResumenDTO resumen;

    private ExtractoCuentaSharedEstadisticaDTO estadisticas;

    private List<ExtractoCuentaSharedMovimientoDTO> movimientos;
}