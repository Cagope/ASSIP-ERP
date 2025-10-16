package co.assip.erp.hojavida.repository;

import co.assip.erp.hojavida.domain.Laboral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LaboralRepository extends JpaRepository<Laboral, Integer> {
    Optional<Laboral> findByIdDatosPersonal(Integer idDatosPersonal);
    boolean existsByIdDatosPersonal(Integer idDatosPersonal);
}
