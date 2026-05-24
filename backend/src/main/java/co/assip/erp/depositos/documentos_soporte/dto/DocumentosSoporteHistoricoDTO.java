package co.assip.erp.depositos.documentos_soporte.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class DocumentosSoporteHistoricoDTO {

    private Integer idDocumentoSoporte;

    private String tipoDocumentoSoporte;

    private String numeroInicial;

    private String numeroFinal;

    private String estadoDocumento;

    private LocalDate fechaEntrega;

    private LocalDate fechaEstado;

    private Integer usuarioCreacion;

    private LocalDateTime fechaCreacion;

}