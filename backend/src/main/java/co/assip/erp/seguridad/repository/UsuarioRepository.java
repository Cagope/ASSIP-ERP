package co.assip.erp.seguridad.repository;

import co.assip.erp.seguridad.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsername(String username);

    // 🔹 Nuevo método: obtener el ID del usuario directamente por username
    @Query(value = "SELECT id_usuario FROM seguridad.usuarios WHERE username = :username", nativeQuery = true)
    Integer findIdByUsername(String username);
}
