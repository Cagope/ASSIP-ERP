package co.assip.erp.hojavida.repository;

import co.assip.erp.hojavida.domain.PermisoEspecial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermisoEspecialRepository extends JpaRepository<PermisoEspecial, Integer> {
    Optional<PermisoEspecial> findByIdDatosPersonal(Integer idDatosPersonal);
    boolean existsByIdDatosPersonal(Integer idDatosPersonal);
    Optional<PermisoEspecial> findByIdPermisoEspecialAndIdDatosPersonal(Integer idPermisoEspecial, Integer idDatosPersonal);
    long deleteByIdPermisoEspecialAndIdDatosPersonal(Integer idPermisoEspecial, Integer idDatosPersonal);
}
