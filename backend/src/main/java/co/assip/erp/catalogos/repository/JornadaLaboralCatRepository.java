package co.assip.erp.catalogos.repository;

import co.assip.erp.catalogos.domain.JornadaLaboralCat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JornadaLaboralCatRepository extends JpaRepository<JornadaLaboralCat, String> {
    List<JornadaLaboralCat> findByNombreJornadaContainingIgnoreCase(String q);
}
