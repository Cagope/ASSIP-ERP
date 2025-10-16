// src/main/java/co/assip/erp/hojavida/repository/ReferenciaPersonalRepository.java
package co.assip.erp.hojavida.repository;

import co.assip.erp.hojavida.domain.ReferenciaPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReferenciaPersonalRepository extends JpaRepository<ReferenciaPersonal, Integer> {

    // Si solo permites 1 referencia por persona, este sirve:
    Optional<ReferenciaPersonal> findByIdDatosPersonal(Integer idDatosPersonal);

    // Para validar pertenencia en update/delete
    Optional<ReferenciaPersonal> findByIdReferenciaPersonalAndIdDatosPersonal(Integer idReferenciaPersonal, Integer idDatosPersonal);

    boolean existsByIdDatosPersonal(Integer idDatosPersonal);

    long deleteByIdReferenciaPersonalAndIdDatosPersonal(Integer idReferenciaPersonal, Integer idDatosPersonal);

    // ===== NUEVO: obtener el nombre de la ciudad por id (desde catalogos.ciudades) =====
    @Query(value = "SELECT c.nombre_ciudad FROM catalogos.ciudades c WHERE c.id_ciudad = :idCiudad", nativeQuery = true)
    String findNombreCiudadById(@Param("idCiudad") Integer idCiudad);
}
