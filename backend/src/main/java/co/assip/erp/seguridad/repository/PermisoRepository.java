package co.assip.erp.seguridad.repository;

import co.assip.erp.seguridad.domain.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {
}
