package co.assip.erp.seguridad.repository;

import co.assip.erp.seguridad.domain.LogEvento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogEventoRepository extends JpaRepository<LogEvento, Long> { }
