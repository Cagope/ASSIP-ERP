package co.assip.erp.general.repository;

import co.assip.erp.general.domain.Privilegiado;
import co.assip.erp.general.dto.PrivilegiadoDTOs.PrivilegiadoListItemDTO;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrivilegiadoRepository extends JpaRepository<Privilegiado, Integer> {

    // === Lista filtrada por Directivo ===
    @Query("""
        select new co.assip.erp.general.dto.PrivilegiadoDTOs$PrivilegiadoListItemDTO(
          pr.idPrivilegiado,
          d.idDirectivo,
          p.idDatosPersonal,
          p.documento,
          case when p.tipoPersona = '2'
               then p.nombres
               else concat(p.nombres,' ',p.primerApellido,' ',coalesce(p.segundoApellido,'')) end,
          pr.codigoParentesco,
          par.nombreParentesco,
          /* campos del directivo: los dejamos null, el front hace fallback */
          null,
          null
        )
        from Privilegiado pr
        join pr.directivo d
        join pr.persona p
        left join co.assip.erp.catalogos.domain.ParentescoCat par
               on par.codigoParentesco = pr.codigoParentesco
        where d.idDirectivo = :idDirectivo
        order by pr.idPrivilegiado desc
    """)
    List<PrivilegiadoListItemDTO> findListItemsByDirectivo(@Param("idDirectivo") Integer idDirectivo);

    // === Lista completa (sin filtrar por directivo) ===
    @Query("""
        select new co.assip.erp.general.dto.PrivilegiadoDTOs$PrivilegiadoListItemDTO(
          pr.idPrivilegiado,
          d.idDirectivo,
          p.idDatosPersonal,
          p.documento,
          case when p.tipoPersona = '2'
               then p.nombres
               else concat(p.nombres,' ',p.primerApellido,' ',coalesce(p.segundoApellido,'')) end,
          pr.codigoParentesco,
          par.nombreParentesco,
          /* campos del directivo: null; el front muestra por fallback */
          null,
          null
        )
        from Privilegiado pr
        join pr.directivo d
        join pr.persona p
        left join co.assip.erp.catalogos.domain.ParentescoCat par
               on par.codigoParentesco = pr.codigoParentesco
        order by pr.idPrivilegiado desc
    """)
    List<PrivilegiadoListItemDTO> findAllListItems();

    boolean existsById(Integer id);
}
