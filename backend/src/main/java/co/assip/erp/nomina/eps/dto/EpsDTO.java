package co.assip.erp.nomina.eps.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EpsDTO {

    private Integer idEps;
    private String nombreEps;

    // En tu tabla existe, lo dejamos para integrarlo con datos_personal
    private Integer idDatosPersonal;

    private Boolean activo;
    private String documento;

}
