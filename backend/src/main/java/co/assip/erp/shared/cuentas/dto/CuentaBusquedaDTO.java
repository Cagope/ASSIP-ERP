package co.assip.erp.shared.cuentas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuentaBusquedaDTO {

    private Integer idCuenta;

    private Integer idAgencia;
    private String nombreAgencia;

    private String codigoCuenta;
    private String nombreCuenta;
}
