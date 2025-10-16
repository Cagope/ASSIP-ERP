package co.assip.erp.seguridad.repository;

import co.assip.erp.seguridad.domain.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface SesionRepository extends JpaRepository<Sesion, UUID> {

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE seguridad.sesiones
           SET revocada = TRUE,
               motivo_revocacion = :motivo
         WHERE id_usuario = :idUsuario
           AND revocada = FALSE
        """, nativeQuery = true)
    int revocarPorUsuario(Long idUsuario, String motivo);
}
