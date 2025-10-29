package co.assip.erp.hojavida.laboral;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * 📦 Repositorio: LaboralRepository
 * Permite CRUD sobre la tabla hoja_vida.laboral
 */
@Repository
public interface LaboralRepository extends JpaRepository<Laboral, Integer> {

    Optional<Laboral> findByIdDatosPersonal(Integer idDatosPersonal);

    boolean existsByIdDatosPersonal(Integer idDatosPersonal);
}
