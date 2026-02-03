package co.assip.erp.nomina.arl.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArlDTO {

    private Integer idArl;
    private String nombreArl;

    private Integer idDatosPersonal;

    // ✅ para mostrar NIT/documento en el listado
    private String documento;

    private Boolean activo;
}
