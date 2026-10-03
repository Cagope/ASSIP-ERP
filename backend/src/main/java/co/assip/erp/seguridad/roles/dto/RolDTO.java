package co.assip.erp.seguridad.roles.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representación de un rol expuesta por la API.
 *
 * Evita exponer directamente entidades administradas
 * por Hibernate en las respuestas REST.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolDTO {

    private Integer idRol;
    private String nombreRol;
    private String descripcion;
    private Boolean activo;
}