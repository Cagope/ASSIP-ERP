package co.assip.erp.depositos.documentos_soporte.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentosSoporteBusquedaDTO {

    private Integer idAgencia;

    private String documento;

    private String nombres;

    private String primerApellido;

    private String segundoApellido;

}