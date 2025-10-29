package co.assip.erp.hojavida.datos_personales;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link DatosPersonales}.
 *
 * Proporciona operaciones CRUD y soporte para consultas personalizadas.
 *
 * Hereda de {@link JpaRepository}, lo que ofrece:
 *  - findAll(), findById(), save(), deleteById(), existsById(), etc.
 *  - Soporte para paginación y ordenamiento.
 *
 * Ejemplo de uso:
 *  List<DatosPersonales> lista = repository.findAll();
 */
@Repository
public interface DatosPersonalesRepository extends JpaRepository<DatosPersonales, Integer> {

    // ==========================================================
    // 🔹 FUTURAS CONSULTAS PERSONALIZADAS
    // ==========================================================

    // Ejemplo:
    // Optional<DatosPersonales> findByDocumento(String documento);

    // Ejemplo:
    // List<DatosPersonales> findByPrimerApellidoContainingIgnoreCase(String apellido);

}
