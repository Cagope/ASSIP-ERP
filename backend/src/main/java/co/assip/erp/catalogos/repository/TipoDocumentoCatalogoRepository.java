package co.assip.erp.catalogos.repository;

import co.assip.erp.catalogos.domain.TipoDocumentoCat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoDocumentoCatalogoRepository extends JpaRepository<TipoDocumentoCat, String> {
}
