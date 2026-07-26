package co.assip.erp.hojavida.condicionesproteccion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio — Condiciones de Protección
 * ------------------------------------------------------------
 * Proporciona acceso a la tabla:
 * hoja_vida.condiciones_proteccion
 */
@Repository
public interface CondicionProteccionRepository
        extends JpaRepository<CondicionProteccion, Long> {

    /**
     * Busca el registro de condiciones de protección
     * asociado a una persona.
     */
    Optional<CondicionProteccion> findByIdDatosPersonal(
            Integer idDatosPersonal
    );

    /**
     * Verifica si una persona ya tiene un registro.
     */
    boolean existsByIdDatosPersonal(
            Integer idDatosPersonal
    );

    /**
     * Verifica si otra condición de protección utiliza
     * el mismo idDatosPersonal.
     *
     * Se utiliza al actualizar para evitar duplicados.
     */
    boolean existsByIdDatosPersonalAndIdCondicionProteccionNot(
            Integer idDatosPersonal,
            Long idCondicionProteccion
    );
}