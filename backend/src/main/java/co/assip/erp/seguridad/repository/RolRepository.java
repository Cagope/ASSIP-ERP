package co.assip.erp.seguridad.repository;

import co.assip.erp.seguridad.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Long> {
}
