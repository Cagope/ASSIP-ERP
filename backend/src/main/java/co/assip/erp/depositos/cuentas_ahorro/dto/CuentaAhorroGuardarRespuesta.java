package co.assip.erp.depositos.cuentas_ahorro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CuentaAhorroGuardarRespuesta {
    private boolean ok;
    private String mensaje;
    private Integer idCuentaAhorro;
}
