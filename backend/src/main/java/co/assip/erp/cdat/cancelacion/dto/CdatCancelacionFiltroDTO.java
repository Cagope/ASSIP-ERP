package co.assip.erp.cdat.cancelacion.dto;

import lombok.Data;

@Data
public class CdatCancelacionFiltroDTO {

    private String documento;
    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String codigoCdat;
}