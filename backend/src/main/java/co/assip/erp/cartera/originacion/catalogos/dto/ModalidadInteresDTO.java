package co.assip.erp.cartera.originacion.catalogos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModalidadInteresDTO {

    private String periodoCodigo;
    private String tipoModalidad;
    private String descripcionModalidadInteres;
    private Integer periodoMeses;
}