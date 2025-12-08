package co.assip.erp.depositos.apertura_cuentas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor     // ⭐ requerido porque el Service crea un objeto vacío
@AllArgsConstructor
public class AperturaCuentasRespuestaDTO {

    private boolean ok;
    private String mensaje;
    private Integer idCuentaAhorro;

    // ⭐ El Service estaba usando setCodigoCuenta(...)
    private String codigoCuenta;
}
