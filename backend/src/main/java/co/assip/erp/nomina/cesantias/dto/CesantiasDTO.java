package co.assip.erp.nomina.cesantias.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CesantiasDTO {

    private Integer idCesantias;
    private String nombreCesantias;

    // relación con datos_personales
    private Integer idDatosPersonal;

    // mismo patrón que ARL y EPS
    private String documento;

    private Boolean activo;
}
