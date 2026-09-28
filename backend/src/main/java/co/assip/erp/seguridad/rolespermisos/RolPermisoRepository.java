package co.assip.erp.seguridad.rolespermisos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de asignaciones entre roles y permisos.
 *
 * Representa la relación muchos-a-muchos almacenada en
 * seguridad.rol_permisos.
 *
 * Esta es la fuente oficial para determinar qué permisos
 * tiene asignado cada rol.
 */
@Repository
public interface RolPermisoRepository
        extends JpaRepository<RolPermiso, RolPermisoId> {

    /**
     * Obtiene todas las asignaciones de permisos de un rol.
     *
     * @param idRol identificador del rol
     * @return relaciones rol-permiso encontradas
     */
    List<RolPermiso> findByRol_IdRol(Integer idRol);

    /**
     * Verifica si un rol tiene asignado un permiso determinado.
     *
     * @param idRol identificador del rol
     * @param codigoPermiso código funcional del permiso
     * @return true si existe la asignación
     */
    boolean existsByRol_IdRolAndPermiso_Codigo(
            Integer idRol,
            String codigoPermiso
    );

    /**
     * Elimina todas las asignaciones correspondientes a un rol.
     *
     * Se utilizará al reemplazar de forma controlada
     * la configuración de permisos de un rol.
     *
     * @param idRol identificador del rol
     */
    void deleteByRol_IdRol(Integer idRol);
}