package co.assip.erp.depositos.informes.documentos_soporte.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentosSoporteInformeRequestDTO {

    private String agencia;
    private String fechaDesde;
    private String fechaHasta;

}