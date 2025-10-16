package co.assip.erp.hojavida.repository;

import co.assip.erp.hojavida.domain.Financiero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinancieroRepository extends JpaRepository<Financiero, Integer> {
    Optional<Financiero> findByIdDatosPersonal(Integer idDatosPersonal);
    boolean existsByIdDatosPersonal(Integer idDatosPersonal);
    Optional<Financiero> findByIdFinancieroAndIdDatosPersonal(Integer idFinanciero, Integer idDatosPersonal);
    long deleteByIdFinancieroAndIdDatosPersonal(Integer idFinanciero, Integer idDatosPersonal);
}
