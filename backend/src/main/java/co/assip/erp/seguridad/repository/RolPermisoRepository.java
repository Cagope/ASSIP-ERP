package co.assip.erp.seguridad.repository;

import co.assip.erp.seguridad.domain.RolPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolPermisoRepository extends JpaRepository<RolPermiso, Integer> {

    /**
     * Lista todas las relaciones de permisos para un rol.
     * Coincide con la propiedad rol.idRol del entity RolPermiso.
     */
    List<RolPermiso> findByRol_IdRol(Integer idRol);

    /**
     * Verifica si un rol tiene asignado un permiso por su código.
     * Coincide con las propiedades rol.idRol y permiso.codigo.
     */
    boolean existsByRol_IdRolAndPermiso_Codigo(Integer idRol, String codigoPermiso);
}
