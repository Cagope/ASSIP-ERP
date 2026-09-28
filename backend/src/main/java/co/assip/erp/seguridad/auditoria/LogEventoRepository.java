package co.assip.erp.seguridad.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de eventos de auditoría almacenados
 * en seguridad.log_evento.
 */
@Repository
public interface LogEventoRepository
        extends JpaRepository<LogEvento, Integer> {
}