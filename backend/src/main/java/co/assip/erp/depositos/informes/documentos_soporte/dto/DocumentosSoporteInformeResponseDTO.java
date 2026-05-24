package co.assip.erp.depositos.informes.documentos_soporte.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DocumentosSoporteInformeResponseDTO {

    private DocumentoSoporteResumenDTO resumen;

    private List<DocumentoSoporteInformeItemDTO> items;

}