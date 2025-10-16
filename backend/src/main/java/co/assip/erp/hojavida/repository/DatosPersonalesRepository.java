package co.assip.erp.hojavida.repository;

import co.assip.erp.hojavida.domain.DatosPersonales;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface DatosPersonalesRepository extends JpaRepository<DatosPersonales, Integer> {

    @Query("""
           select d
           from DatosPersonales d
           where (:q is null or :q = '' 
                  or lower(d.documento) like lower(concat('%', :q, '%'))
                  or lower(d.nombres) like lower(concat('%', :q, '%'))
                  or lower(d.primerApellido) like lower(concat('%', :q, '%')))
           """)
    Page<DatosPersonales> search(@Param("q") String q, Pageable pageable);

    @Query("""
           select (count(d) > 0)
           from DatosPersonales d
           where upper(d.tipoDocumento) = upper(:tipoDocumento)
             and d.documento = :documento
           """)
    boolean existsByTipoDocumentoAndDocumento(@Param("tipoDocumento") String tipoDocumento,
                                              @Param("documento") String documento);
}
