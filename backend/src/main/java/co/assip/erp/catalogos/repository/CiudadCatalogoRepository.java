package co.assip.erp.catalogos.repository;

import co.assip.erp.catalogos.domain.CiudadCat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CiudadCatalogoRepository extends JpaRepository<CiudadCat, Integer> { }
