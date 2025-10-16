package co.assip.erp.general.repository;

import co.assip.erp.general.domain.Directivo;
import co.assip.erp.general.dto.DirectivoDTOs.DirectivoListItemDTO;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DirectivoRepository extends JpaRepository<Directivo, Integer> {

    // Lista plana (proyección DTO) – incluye actaAsamblea, fechaAsamblea, resolucionSes
    @Query("""
    select new co.assip.erp.general.dto.DirectivoDTOs$DirectivoListItemDTO(
      d.idDirectivo,
      p.idDatosPersonal,
      p.documento,
      case when p.tipoPersona = '2'
           then p.nombres
           else concat(p.nombres,' ',p.primerApellido,' ',coalesce(p.segundoApellido,''))
      end,
      d.codigoTipoDirectivo,
      d.calidadDirectivo,
      d.estadoDirectivo,
      d.actaAsamblea,
      d.fechaAsamblea,
      d.resolucionSes,
      d.fechaResolucion,
      d.fechaRetiro,
      coalesce(d.periodosVigencia, 0)
    )
    from Directivo d
    join d.persona p
    order by d.idDirectivo desc
    """)
    List<DirectivoListItemDTO> findListItems();

    // Búsqueda por documento/nombre (JPQL, case-insensitive) – incluye los 3 campos
    @Query("""
    select new co.assip.erp.general.dto.DirectivoDTOs$DirectivoListItemDTO(
      d.idDirectivo,
      p.idDatosPersonal,
      p.documento,
      case when p.tipoPersona = '2'
           then p.nombres
           else concat(p.nombres,' ',p.primerApellido,' ',coalesce(p.segundoApellido,''))
      end,
      d.codigoTipoDirectivo,
      d.calidadDirectivo,
      d.estadoDirectivo,
      d.actaAsamblea,
      d.fechaAsamblea,
      d.resolucionSes,
      d.fechaResolucion,
      d.fechaRetiro,
      coalesce(d.periodosVigencia, 0)
    )
    from Directivo d
    join d.persona p
    where lower(p.documento) like lower(concat('%', :q, '%'))
       or lower(p.nombres) like lower(concat('%', :q, '%'))
       or lower(p.primerApellido) like lower(concat('%', :q, '%'))
       or lower(coalesce(p.segundoApellido,'')) like lower(concat('%', :q, '%'))
    order by d.idDirectivo desc
    """)
    List<DirectivoListItemDTO> searchListItemsByPersona(@Param("q") String q);

    boolean existsById(Integer id);
}
