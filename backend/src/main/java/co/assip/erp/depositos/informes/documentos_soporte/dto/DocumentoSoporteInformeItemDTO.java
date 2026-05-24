package co.assip.erp.depositos.informes.documentos_soporte.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentoSoporteInformeItemDTO {

    private Integer idDocumentoSoporte;

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    private Integer idFormaAhorro;
    private String codigoForma;
    private String nombreForma;

    private Integer idCuentaAhorro;
    private String codigoCuenta;

    private String documento;
    private String nombreCompleto;

    private String tipoDocumentoSoporte;
    private String descripcionSoporte;

    private String numeroInicial;
    private String numeroFinal;
    private Long cantidadDocumentos;

    private String estadoDocumento;
    private String descripcionEstado;

    private String fechaEntrega;
    private String fechaEstado;
    private String fechaCreacion;

}