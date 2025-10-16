package co.assip.erp.hojavida.repository;

import co.assip.erp.hojavida.domain.DatosFamiliares;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface DatosFamiliaresRepository extends JpaRepository<DatosFamiliares, Integer> {

    @Query("""
           select f
           from DatosFamiliares f
           where f.idDatosPersonal = :idPersona
             and (
               :q is null or :q = '' or
               lower(f.nombreDatosFamiliar) like lower(concat('%', :q, '%')) or
               lower(f.documentoDatosFamiliar) like lower(concat('%', :q, '%')) or
               lower(f.codigoParentesco) like lower(concat('%', :q, '%'))
             )
           """)
    Page<DatosFamiliares> searchByPersona(@Param("idPersona") Integer idPersona,
                                          @Param("q") String q,
                                          Pageable pageable);
}
