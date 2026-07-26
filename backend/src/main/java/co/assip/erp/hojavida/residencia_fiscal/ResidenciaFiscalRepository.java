package co.assip.erp.hojavida.residencia_fiscal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 🗂️ Repositorio JPA — Residencia Fiscal
 * ------------------------------------------------------------
 * Gestiona las operaciones CRUD sobre la tabla
 * hoja_vida.sarlaft_residencia_fiscal.
 *
 * Cada persona puede registrar únicamente una residencia
 * fiscal (relación 1:1).
 */
@Repository
public interface ResidenciaFiscalRepository
        extends JpaRepository<ResidenciaFiscal, Long> {

    /**
     * 🔹 Retorna todos los registros ordenados por
     * fecha de edición descendente.
     */
    List<ResidenciaFiscal> findAllByOrderByFechaEdicionDesc();

    /**
     * 🔹 Busca la residencia fiscal asociada a una persona.
     *
     * @param idDatosPersonal Identificador de la persona.
     * @return Residencia fiscal registrada, si existe.
     */
    Optional<ResidenciaFiscal> findByIdDatosPersonal(
            Integer idDatosPersonal
    );

}