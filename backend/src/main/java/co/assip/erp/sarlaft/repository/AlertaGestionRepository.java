package co.assip.erp.sarlaft.repository;

import co.assip.erp.sarlaft.domain.AlertaGestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertaGestionRepository extends JpaRepository<AlertaGestion, Long> {
}
