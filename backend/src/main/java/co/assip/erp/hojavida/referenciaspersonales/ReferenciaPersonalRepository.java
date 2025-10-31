package co.assip.erp.hojavida.referenciaspersonales;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 🗂️ Repositorio JPA — Referencias Personales
 * ------------------------------------------------------------
 * Gestiona el acceso a los datos de la tabla hoja_vida.referencias_personales
 * a través de Spring Data JPA.
 *
 * Ofrece consultas automáticas para búsqueda por persona o ubicación geográfica.
 */
@Repository
public interface ReferenciaPersonalRepository extends JpaRepository<ReferenciaPersonal, Integer> {

    /**
     * 🔹 Retorna todas las referencias personales ordenadas por fecha de edición descendente.
     */
    List<ReferenciaPersonal> findAllByOrderByFechaEdicionDesc();

    /**
     * 🔹 Busca todas las referencias asociadas a un ID de persona.
     * @param idDatosPersonal identificador de la persona.
     */
    List<ReferenciaPersonal> findByIdDatosPersonal(Integer idDatosPersonal);

    /**
     * 🔹 Busca referencias filtrando por departamento (opcional para reportes regionales).
     * @param idDepartamento identificador del departamento.
     */
    List<ReferenciaPersonal> findByIdDepartamento(Integer idDepartamento);

    /**
     * 🔹 Busca referencias filtrando por ciudad.
     * @param idCiudad identificador de la ciudad.
     */
    List<ReferenciaPersonal> findByIdCiudad(Integer idCiudad);
}
