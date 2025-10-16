package co.assip.erp.catalogos.repository;

import co.assip.erp.catalogos.domain.TipoPepsCat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoPepsCatalogoRepository extends JpaRepository<TipoPepsCat, String> {
    List<TipoPepsCat> findAllByOrderByNombreTipoPepsAsc();
}
