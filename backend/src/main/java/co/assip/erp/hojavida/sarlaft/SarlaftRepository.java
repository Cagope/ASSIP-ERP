package co.assip.erp.hojavida.sarlaft;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 🗂️ Repositorio JPA — Sarlaft
 * Gestiona operaciones CRUD sobre la tabla hoja_vida.sarlaft.
 */
@Repository
public interface SarlaftRepository extends JpaRepository<Sarlaft, Integer> {

    /**
     * 🔹 Retorna todos los registros SARLAFT ordenados por fecha de edición descendente.
     */
    List<Sarlaft> findAllByOrderByFechaEdicionDesc();

    /**
     * 🔹 Busca un registro SARLAFT asociado a un ID de persona.
     * Cada persona puede tener solo un registro SARLAFT (relación 1:1).
     * @param idDatosPersonal identificador de la persona.
     */
    Optional<Sarlaft> findByIdDatosPersonal(Integer idDatosPersonal);
}
