package co.assip.erp.cdat.cdats.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CdatBeneficiarioDTO {

    private Long idBeneficiarioCdat;

    private Long idCuentaCdat;

    private String documento;

    private String nombre;

    private String telefono;

    private String tipoParentesco;

    private String estado;
}