package co.assip.erp.depositos.informes.extracto_cuenta.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExtractoCuentaResponseDTO {

    private ExtractoCuentaResumenDTO resumen;

    private List<ExtractoCuentaMovimientoDTO> movimientos;

}