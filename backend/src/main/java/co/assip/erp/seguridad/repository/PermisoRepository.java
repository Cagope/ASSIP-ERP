package co.assip.erp.seguridad.repository;

import co.assip.erp.seguridad.domain.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermisoRepository extends JpaRepository<Permiso, Integer> {

    Optional<Permiso> findByCodigo(String codigo);

    List<Permiso> findByRol_IdRol(Integer idRol);
}
