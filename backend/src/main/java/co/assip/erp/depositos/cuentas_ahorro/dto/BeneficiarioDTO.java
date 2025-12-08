package co.assip.erp.depositos.cuentas_ahorro.dto;

import lombok.Data;

@Data
public class BeneficiarioDTO {
    private Integer idBeneficiario;
    private Integer idCuentaAhorro;
    private String documentoBeneficiario;
    private String nombreBeneficiario;
    private String telefonoBeneficiario;
    private String celularBeneficiario;
    private String tipoParentesco;
}
