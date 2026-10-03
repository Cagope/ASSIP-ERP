package co.assip.erp.seguridad.rolespermisos.dto;

/**
 * Representa un permiso asignado a un rol
 * para exposición mediante la API REST.
 *
 * Evita exponer directamente entidades JPA
 * y proxies de Hibernate fuera de la capa de persistencia.
 */
public record RolPermisoDTO(
        Integer idPermiso,
        String codigo,
        String descripcion,
        Boolean activo
) {
}