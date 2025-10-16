package co.assip.erp.hojavida.repository;

import co.assip.erp.hojavida.domain.Sarlaft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SarlaftRepository extends JpaRepository<Sarlaft, Integer> {
    Optional<Sarlaft> findByIdDatosPersonal(Integer idDatosPersonal);
    boolean existsByIdDatosPersonal(Integer idDatosPersonal);
    Optional<Sarlaft> findByIdSarlaftAndIdDatosPersonal(Integer idSarlaft, Integer idDatosPersonal);
    long deleteByIdSarlaftAndIdDatosPersonal(Integer idSarlaft, Integer idDatosPersonal);
}
