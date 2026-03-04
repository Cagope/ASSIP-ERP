package co.assip.erp.nomina.desprendible.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DesprendibleEmpleadoListDTO {

    private Integer idEmpleado;
    private Integer idContrato;

    private String documento;
    private String nombreCompleto;
    private String cargo;

}