package co.assip.erp.catalogos.repository;

import co.assip.erp.catalogos.domain.ParentescoCat;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParentescoCatalogoRepository extends JpaRepository<ParentescoCat, String> {

    @Query("""
           select p
           from ParentescoCat p
           where (:q is null or :q = ''
                  or lower(p.codigoParentesco) like lower(concat('%', :q, '%'))
                  or lower(p.nombreParentesco) like lower(concat('%', :q, '%')))
           order by p.nombreParentesco asc
           """)
    List<ParentescoCat> search(@Param("q") String q);
}
