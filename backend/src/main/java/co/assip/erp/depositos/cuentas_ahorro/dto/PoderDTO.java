package co.assip.erp.depositos.cuentas_ahorro.dto;

import lombok.Data;

@Data
public class PoderDTO {
    private Integer idPoder;
    private Integer idCuenta;
    private String documentoPoder;
    private String nombrePoder;
    private String telefonoPoder;
    private String celularPoder;
    private String correoPoder;
}
