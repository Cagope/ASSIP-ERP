package co.assip.erp.cartera.catalogos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarteraCatalogoDTO {

    /**
     * Identificador numérico.
     * Solo aplica para catálogos que utilizan una PK numérica.
     */
    private Long id;

    /**
     * Código del catálogo.
     */
    private String codigo;

    /**
     * Nombre o descripción.
     */
    private String nombre;

    /**
     * Estado del registro.
     */
    private Boolean activo;

}