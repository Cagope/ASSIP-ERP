package co.assip.erp.cartera.catalogos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CentralRiesgoDTO {

    private Integer idCentralRiesgo;

    private String codigoCentral;

    private String documentoCentral;

    private String nombreCentral;

    private String descripcion;

    private Boolean activo;
}