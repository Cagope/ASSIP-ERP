package co.assip.erp.cdat.cdats.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CdatAsociadoValidacionDTO {

    private Integer idDatosPersonal;
    private String documento;
    private String nombreCompleto;

    private Boolean tieneAportesActivos;
    private Boolean datosActualizados;

    private String mensajeError;
}