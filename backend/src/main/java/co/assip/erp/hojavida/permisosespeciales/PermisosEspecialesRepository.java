package co.assip.erp.hojavida.permisosespeciales;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * 🗂️ Repositorio JPA — Permisos Especiales
 * Gestiona operaciones CRUD sobre la tabla hoja_vida.permisos_especiales.
 */
@Repository
public interface PermisosEspecialesRepository extends JpaRepository<PermisosEspeciales, Integer> {

    /**
     * 🔹 Retorna todos los registros de permisos especiales
     * ordenados por fecha de edición descendente.
     */
    List<PermisosEspeciales> findAllByOrderByFechaEdicionDesc();

    /**
     * 🔹 Busca el registro de permisos especiales asociado
     * a un ID de persona. Cada persona puede tener solo un
     * registro (relación 1:1).
     * @param idDatosPersonal identificador de la persona.
     */
    Optional<PermisosEspeciales> findByIdDatosPersonal(Integer idDatosPersonal);
}
