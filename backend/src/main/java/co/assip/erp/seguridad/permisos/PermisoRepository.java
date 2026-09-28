package co.assip.erp.seguridad.permisos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio del catálogo de permisos del sistema.
 *
 * Administra exclusivamente los permisos disponibles en el ERP.
 * La asignación de permisos a roles se gestiona mediante
 * seguridad.rol_permisos.
 */
@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Integer> {

    /**
     * Busca un permiso por su código único.
     *
     * @param codigo código funcional del permiso
     * @return permiso encontrado, si existe
     */
    Optional<Permiso> findByCodigo(String codigo);
}