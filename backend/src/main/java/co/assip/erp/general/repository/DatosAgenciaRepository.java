package co.assip.erp.general.repository;

import co.assip.erp.general.domain.DatosAgencia;
import co.assip.erp.general.dto.AgenciaDTOs.AgenciaListItemDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DatosAgenciaRepository extends JpaRepository<DatosAgencia, Integer> {

    boolean existsByCodigoAgenciaIgnoreCase(String codigoAgencia);

    Optional<DatosAgencia> findByCodigoAgenciaIgnoreCase(String codigoAgencia);

    // <-- Mantén este método porque tu Servicio lo invoca en la rama de búsqueda
    List<DatosAgencia> findByNombreAgenciaContainingIgnoreCase(String q);

    // Listado “blindado” con la sigla correcta directamente en el DTO
    @Query("""
        select new co.assip.erp.general.dto.AgenciaDTOs$AgenciaListItemDTO(
            a.idAgencia, a.codigoAgencia, a.nombreAgencia, a.siglaAgencia
        )
        from DatosAgencia a
        order by a.idAgencia asc
    """)
    List<AgenciaListItemDTO> findListItems();

    // Búsqueda también “blindada” retornando el DTO con sigla correcta
    @Query("""
        select new co.assip.erp.general.dto.AgenciaDTOs$AgenciaListItemDTO(
            a.idAgencia, a.codigoAgencia, a.nombreAgencia, a.siglaAgencia
        )
        from DatosAgencia a
        where lower(a.nombreAgencia) like lower(concat('%', :q, '%'))
        order by a.idAgencia asc
    """)
    List<AgenciaListItemDTO> searchListItemsByNombre(@Param("q") String q);
}
