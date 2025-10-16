package co.assip.erp.catalogos.repository;

import co.assip.erp.catalogos.domain.TiposContratoCat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TiposContratoCatRepository extends JpaRepository<TiposContratoCat, String> {
    List<TiposContratoCat> findByNombreTipoContratoContainingIgnoreCase(String q);
}
