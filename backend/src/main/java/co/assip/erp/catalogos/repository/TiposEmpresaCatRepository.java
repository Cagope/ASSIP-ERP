package co.assip.erp.catalogos.repository;

import co.assip.erp.catalogos.domain.TiposEmpresaCat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TiposEmpresaCatRepository extends JpaRepository<TiposEmpresaCat, String> {
    List<TiposEmpresaCat> findByNombreTipoEmpresaContainingIgnoreCase(String q);
}
