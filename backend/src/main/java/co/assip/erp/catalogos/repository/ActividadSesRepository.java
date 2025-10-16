package co.assip.erp.catalogos.repository;

import co.assip.erp.catalogos.domain.ActividadEconomicaSes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActividadSesRepository extends JpaRepository<ActividadEconomicaSes, String> {
    List<ActividadEconomicaSes> findByCodigoActividadSesContainingIgnoreCaseOrNombreActividadSesContainingIgnoreCase(
            String codigo, String nombre, Pageable pageable
    );
}
