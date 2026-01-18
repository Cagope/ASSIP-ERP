package co.assip.erp.shared.personas.dto;

import lombok.Data;

@Data
public class PersonaBusquedaDTO {
    private Long idDatosPersonal;
    private String documento;
    private String nombreCompleto;
}
