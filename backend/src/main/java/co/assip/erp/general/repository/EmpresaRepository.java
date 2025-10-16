package co.assip.erp.general.repository;

import co.assip.erp.general.domain.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, String> {
    // Documento es la PK (String)
}
