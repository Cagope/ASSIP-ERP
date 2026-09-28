package co.assip.erp.seguridad.usuarios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio de usuarios del sistema.
 *
 * Centraliza las consultas sobre seguridad.usuarios utilizadas
 * por autenticación, recuperación de contraseña y administración
 * de usuarios.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca un usuario por su nombre de acceso.
     * Utilizado principalmente durante la autenticación.
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Verifica si ya existe un nombre de usuario.
     */
    boolean existsByUsername(String username);

    /**
     * Busca un usuario por su correo electrónico.
     * Utilizado en procesos de recuperación de contraseña.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca un usuario mediante el token de recuperación
     * de contraseña.
     */
    Optional<Usuario> findByTokenRecuperacion(String token);

    /**
     * Obtiene únicamente el identificador interno de un usuario
     * a partir de su username.
     */
    @Query(value = """
            SELECT id_usuario
            FROM seguridad.usuarios
            WHERE username = :username
            """,
            nativeQuery = true)
    Integer findIdByUsername(String username);

    /**
     * Obtiene el listado administrativo de usuarios incluyendo
     * la información básica de su rol y agencia principal.
     */
    @Query(value = """
            SELECT
                u.id_usuario AS idUsuario,
                u.username AS username,
                u.nombre_completo AS nombreCompleto,
                u.email AS email,
                u.activo AS activo,
                r.id_rol AS idRol,
                r.nombre_rol AS nombreRol,
                u.id_agencia_principal AS idAgenciaPrincipal
            FROM seguridad.usuarios u
            LEFT JOIN seguridad.roles r
                ON r.id_rol = u.id_rol
            ORDER BY u.username
            """,
            nativeQuery = true)
    List<Map<String, Object>> listarConRolYAgencia();

    /**
     * Obtiene la información administrativa de un usuario
     * para su consulta o edición.
     *
     * Mantiene el mismo formato utilizado por el listado general.
     */
    @Query(value = """
            SELECT
                u.id_usuario AS idUsuario,
                u.username AS username,
                u.nombre_completo AS nombreCompleto,
                u.email AS email,
                u.activo AS activo,
                r.id_rol AS idRol,
                r.nombre_rol AS nombreRol,
                u.id_agencia_principal AS idAgenciaPrincipal
            FROM seguridad.usuarios u
            LEFT JOIN seguridad.roles r
                ON r.id_rol = u.id_rol
            WHERE u.id_usuario = :idUsuario
            """,
            nativeQuery = true)
    Map<String, Object> buscarDtoPorId(Integer idUsuario);
}