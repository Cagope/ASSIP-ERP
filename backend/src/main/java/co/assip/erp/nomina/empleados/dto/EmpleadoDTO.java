package co.assip.erp.nomina.empleados.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpleadoDTO {

    private Integer idEmpleado;
    private Integer idAgencia;
    private Integer idDatosPersonal;
    private Boolean activo;
}
