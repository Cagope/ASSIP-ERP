package co.assip.erp.catalogos.repository;

import co.assip.erp.catalogos.domain.ActividadEconomicaDian;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActividadDianRepository extends JpaRepository<ActividadEconomicaDian, String> {
    List<ActividadEconomicaDian> findByCodigoActividadDianContainingIgnoreCaseOrNombreActividadDianContainingIgnoreCase(
            String codigo, String nombre, Pageable pageable
    );
}
