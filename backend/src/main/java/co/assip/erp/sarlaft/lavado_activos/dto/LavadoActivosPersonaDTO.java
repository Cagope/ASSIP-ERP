package co.assip.erp.sarlaft.lavado_activos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LavadoActivosPersonaDTO {

    private Long idDatosPersonal;

    private String tipoDocumento;
    private String documento;

    private Integer idDepartamentoExpedicion;
    private Integer idCiudadExpedicion;

    private String primerApellido;
    private String segundoApellido;

    private String primerNombre;
    private String segundoNombre;

    private String nombreCompleto;

    private String direccion;
    private String telefono;
}