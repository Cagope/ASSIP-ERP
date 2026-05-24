package co.assip.erp.depositos.informes.documentos_soporte.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentoSoporteResumenAgenciaFormaDTO {

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    private Integer idFormaAhorro;
    private String codigoForma;
    private String nombreForma;

    private String tipoDocumentoSoporte;
    private String descripcionSoporte;

    private Long totalDocumentos;
    private Long activos;
    private Long inactivos;
    private Long perdidos;
    private Long robados;
    private Long totalDocumentosFisicos;

}