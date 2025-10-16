package co.assip.erp.seguridad.repository;

import co.assip.erp.seguridad.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Page<Usuario> findByUsernameContainingIgnoreCase(String q, Pageable pageable);

    Page<Usuario> findByActivoAndUsernameContainingIgnoreCase(boolean activo, String q, Pageable pageable);

    Page<Usuario> findBySuperuserSeguridadAndUsernameContainingIgnoreCase(boolean superuser, String q, Pageable pageable);

    Page<Usuario> findByActivoAndSuperuserSeguridadAndUsernameContainingIgnoreCase(boolean activo, boolean superuser, String q, Pageable pageable);

    boolean existsByUsername(String username);

    // ⬇️ ESTE ES EL QUE AGREGAMOS PARA AUTH (login)
    Optional<Usuario> findByUsernameIgnoreCase(String username);
}
