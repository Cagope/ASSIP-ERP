package co.assip.erp.seguridad.repository;

import co.assip.erp.seguridad.domain.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermisoRepository extends JpaRepository<Permiso, Integer> {

    Optional<Permiso> findByCodigo(String codigo);

    // ✔ Buscar permisos por rol usando id_rol (NO relaciones)
    List<Permiso> findByIdRol(Integer idRol);

    // ✔ Verificar permisos directamente usando id_rol + codigo
    boolean existsByIdRolAndCodigo(Integer idRol, String codigo);
}
