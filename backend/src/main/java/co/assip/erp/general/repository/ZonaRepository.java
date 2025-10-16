package co.assip.erp.general.repository;

import co.assip.erp.general.domain.Zona;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZonaRepository extends JpaRepository<Zona, Integer> {
    Page<Zona> findByNombreZonaContainingIgnoreCase(String q, Pageable pageable);
    Page<Zona> findByCodigoZonaContainingIgnoreCase(String q, Pageable pageable);

    boolean existsByCodigoZona(String codigoZona);
    boolean existsByCodigoZonaAndIdZonaNot(String codigoZona, Integer idZona);
}
