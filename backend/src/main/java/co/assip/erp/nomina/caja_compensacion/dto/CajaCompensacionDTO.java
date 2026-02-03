package co.assip.erp.nomina.caja_compensacion.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CajaCompensacionDTO {

    private Integer idCaja;
    private String nombreCaja;

    private Integer idDatosPersonal;

    // ✅ para mostrar documento en el listado
    private String documento;

    private Boolean activo;
}
