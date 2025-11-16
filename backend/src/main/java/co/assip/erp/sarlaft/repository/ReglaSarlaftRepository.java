package co.assip.erp.sarlaft.repository;

import co.assip.erp.sarlaft.domain.ReglaSarlaft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReglaSarlaftRepository extends JpaRepository<ReglaSarlaft, Long> {

    // Útil para el motor SARLAFT (AlertaEngine)
    ReglaSarlaft findByCodigo(String codigo);
}
