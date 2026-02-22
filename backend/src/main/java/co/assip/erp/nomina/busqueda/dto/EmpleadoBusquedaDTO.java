package co.assip.erp.nomina.busqueda.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpleadoBusquedaDTO {

    private Integer idEmpleado;
    private Long idDatosPersonal;

    private String documento;
    private String nombreCompleto;

    private Integer idAgencia;
    private String nombreAgencia;

    private Integer idContratoActivo;
    private String nombreCargo;

    private Boolean activo;
}
