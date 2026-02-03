package co.assip.erp.nomina.afp.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfpDTO {

    private Integer idAfp;
    private String nombreAfp;

    private Integer idDatosPersonal;

    // ✅ para mostrar NIT/documento en el listado
    private String documento;

    private Boolean activo;
}
