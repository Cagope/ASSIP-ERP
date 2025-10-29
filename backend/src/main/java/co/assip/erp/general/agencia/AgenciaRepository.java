package co.assip.erp.general.agencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 📘 Repositorio JPA para la entidad Agencia.
 * Permite operaciones CRUD estándar sobre general.datos_agencias.
 */
@Repository
public interface AgenciaRepository extends JpaRepository<Agencia, Integer> {
}
