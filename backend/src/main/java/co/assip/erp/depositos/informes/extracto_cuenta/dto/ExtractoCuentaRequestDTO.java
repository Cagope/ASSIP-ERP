package co.assip.erp.depositos.informes.extracto_cuenta.dto;

import lombok.Data;

@Data
public class ExtractoCuentaRequestDTO {

    private Integer idCuentaAhorro;

    private String fechaInicial;
    private String fechaFinal;

}