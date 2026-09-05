package co.assip.erp.gerencia.expedienteasociado.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpedientePersonaBusquedaDTO {

    private Long idDatosPersonal;

    private String tipoDocumento;
    private String nombreTipoDocumento;
    private String documento;

    private String nombres;
    private String primerApellido;
    private String segundoApellido;
    private String nombreCompleto;
}