package co.assip.erp.depositos.documentos_soporte.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentosSoporteResponseDTO {

    private boolean success;

    private String message;

    private Integer idDocumentoSoporte;

}