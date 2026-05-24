package co.assip.erp.depositos.informes.extracto_asociado.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExtractoAsociadoResponseDTO {

    private ExtractoAsociadoResumenDTO resumen;

    private List<ExtractoAsociadoCuentaDTO> cuentas;

    private List<ExtractoAsociadoMovimientoDTO> movimientos;

}