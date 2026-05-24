package co.assip.erp.depositos.informes.extracto_asociado.dto;

import lombok.Data;

@Data
public class ExtractoAsociadoRequestDTO {

    private Integer idDatosPersonal;

    private String fechaInicial;
    private String fechaFinal;

    /**
     * "0" = todas las formas.
     */
    private String codigoForma;

}