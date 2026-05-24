package co.assip.erp.depositos.documentos_soporte.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DocumentosSoporteFormDTO {

    private Integer idDocumentoSoporte;

    private String tipoDocumentoSoporte;

    private String numeroInicial;

    private String numeroFinal;

    private LocalDate fechaEntrega;

    private String estadoDocumento;

    private LocalDate fechaEstado;

}