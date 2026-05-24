package co.assip.erp.shared.cuentas_ahorro.extracto.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtractoCuentaSharedRequestDTO {

    private Integer idCuentaAhorro;

    private String fechaInicial;
    private String fechaFinal;
}