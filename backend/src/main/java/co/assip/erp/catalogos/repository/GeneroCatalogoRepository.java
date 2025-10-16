package co.assip.erp.catalogos.repository;

import co.assip.erp.catalogos.domain.GeneroCat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneroCatalogoRepository extends JpaRepository<GeneroCat, String> { }
