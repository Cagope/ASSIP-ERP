package co.assip.erp.seguridad.repository;

import co.assip.erp.seguridad.domain.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Integer> {
}
