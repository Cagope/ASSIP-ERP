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
     * Indica si la línea de crédito corresponde a utilizaciones
     * de un cupo de tarjeta.
     *
     * Solo aplica para el catálogo de líneas de crédito.
     */
    private Boolean esUtilizacionCupoTarjeta;

    /**
     * Estado del registro.
     */
    private Boolean activo;
}