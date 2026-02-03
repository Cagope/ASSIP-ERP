package co.assip.erp.nomina.conceptos_nomina.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptoNominaDTO {

    private String codigo;
    private String nombre;
    private String tipo;

    private Boolean esFijo;
    private Boolean activo;
}
