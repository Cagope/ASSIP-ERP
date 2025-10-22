package co.assip.erp.seguridad.repository;

import co.assip.erp.seguridad.domain.LogEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de eventos de log (seguridad.log_evento).
 * Permite registrar y consultar actividades del sistema.
 */
@Repository
public interface LogEventoRepository extends JpaRepository<LogEvento, Integer> {
}
