package co.assip.erp.hojavida.laboral;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 🗂️ Repositorio JPA — Laborales
 * Gestiona operaciones CRUD sobre la tabla hoja_vida.laborales.
 */
@Repository
public interface LaboralRepository extends JpaRepository<Laboral, Integer> {

    /**
     * 🔹 Retorna todos los registros laborales ordenados por fecha de edición descendente.
     */
    List<Laboral> findAllByOrderByFechaEdicionDesc();

    /**
     * 🔹 Busca un registro laboral asociado a un ID de persona.
     * Cada persona puede tener solo un registro laboral (relación 1:1).
     * @param idDatosPersonal identificador de la persona.
     */
    Optional<Laboral> findByIdDatosPersonal(Integer idDatosPersonal);
}
