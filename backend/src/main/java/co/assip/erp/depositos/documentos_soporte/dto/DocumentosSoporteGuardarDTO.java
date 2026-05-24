package co.assip.erp.depositos.documentos_soporte.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DocumentosSoporteGuardarDTO {

    private String accion;

    private Integer idCuentaAhorro;

    private String numeroInicial;

    private LocalDate fechaEntrega;

    private String estadoDocumento;

    private String observacion;

}