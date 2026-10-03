
package co.assip.erp.seguridad.usuarios;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio principal de cuentas de usuario.
 *
 * Administra exclusivamente la información almacenada
 * en seguridad.usuarios.
 *
 * Los roles del usuario se administran mediante
 * seguridad.usuarios_roles.
 *
 * Incorpora bloqueo pesimista para operaciones
 * concurrentes de seguridad y autenticación.
 */
@Repository
public interface UsuarioRepository
        extends JpaRepository<Usuario, Integer> {

    /**
     * Busca un usuario por su nombre de acceso.
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Verifica si existe un nombre de usuario.
     */
    boolean existsByUsername(String username);

    /**
     * Busca un usuario por correo electrónico.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca un usuario mediante su token temporal
     * de recuperación de contraseña.
     *
     * Se conserva por compatibilidad mientras
     * finaliza la migración de tokens.
     */
    Optional<Usuario> findByTokenRecuperacion(String token);

    /**
     * Obtiene los usuarios ordenados por nombre de acceso.
     */
    List<Usuario> findAllByOrderByUsernameAsc();

    /**
     * Obtiene el identificador interno correspondiente
     * a un nombre de usuario.
     */
    @Query(
            value = """
                    SELECT id_usuario
                    FROM seguridad.usuarios
                    WHERE username = :username
                    """,
            nativeQuery = true
    )
    Integer findIdByUsername(
            @Param("username") String username
    );

    /**
     * Obtiene un usuario aplicando bloqueo exclusivo
     * sobre su fila durante la transacción.
     *
     * Evita que dos intentos fallidos concurrentes
     * lean y actualicen el mismo contador sin
     * considerar los cambios del otro.
     *
     * Debe ejecutarse dentro de una transacción activa.
     *
     * @param idUsuario identificador del usuario
     * @return usuario bloqueado para actualización
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT u
            FROM Usuario u
            WHERE u.idUsuario = :idUsuario
            """)
    Optional<Usuario> findByIdForUpdate(
            @Param("idUsuario") Integer idUsuario
    );
}
