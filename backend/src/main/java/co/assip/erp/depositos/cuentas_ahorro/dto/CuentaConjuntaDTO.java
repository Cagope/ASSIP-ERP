package co.assip.erp.depositos.cuentas_ahorro.dto;

import lombok.Data;

@Data
public class CuentaConjuntaDTO {

    private Integer idCuentaConjunta;
    private Integer idCuentaAhorro;
    private Integer idDatosPersonal;

    private String documento;
    private String nombreCompleto;

    private String codigoAccion;
    private String descripcionAccion;
}