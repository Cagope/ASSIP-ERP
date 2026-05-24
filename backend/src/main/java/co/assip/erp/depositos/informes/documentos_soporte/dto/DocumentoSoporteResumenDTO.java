package co.assip.erp.depositos.informes.documentos_soporte.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentoSoporteResumenDTO {

    private Long totalDocumentos;
    private Long activos;
    private Long inactivos;
    private Long perdidos;
    private Long robados;
    private Long totalDocumentosFisicos;

}