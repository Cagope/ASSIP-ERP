package co.assip.erp.general.subzona;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubZonaRepository extends JpaRepository<SubZona, Integer> {

    boolean existsByCodigoSubZonaAndZona_IdZona(String codigoSubZona, Integer idZona);

}
