package co.assip.erp.seguridad.repository;

import co.assip.erp.seguridad.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // ------------------------------------------------------------
    // 🔹 Búsqueda estándar por username (para login)
    // ------------------------------------------------------------
    Optional<Usuario> findByUsername(String username);

    // ------------------------------------------------------------
    // 🔹 Validar existencia de username (para evitar duplicados)
    // ------------------------------------------------------------
    boolean existsByUsername(String username);

    // ------------------------------------------------------------
    // 🔹 Buscar por correo (reset de contraseña)
    // ------------------------------------------------------------
    Optional<Usuario> findByEmail(String email);

    // ------------------------------------------------------------
    // 🔹 Buscar por token de recuperación
    // ------------------------------------------------------------
    Optional<Usuario> findByTokenRecuperacion(String token);

    // ------------------------------------------------------------
    // 🔹 Obtener solo el ID (legacy, no se toca)
    // ------------------------------------------------------------
    @Query(value = "SELECT id_usuario FROM seguridad.usuarios WHERE username = :username", nativeQuery = true)
    Integer findIdByUsername(String username);

    // ------------------------------------------------------------
    // 🔹 Listado para el frontend (JOIN con rol y agencia)
    // ------------------------------------------------------------
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
        LEFT JOIN seguridad.roles r ON r.id_rol = u.id_rol
        ORDER BY u.username
        """,
            nativeQuery = true)
    List<Map<String, Object>> listarConRolYAgencia();


    // ============================================================
    // 🔥 NUEVO — DTO COMPLETO PARA EDICIÓN
    //     Este método es la CLAVE que faltaba
    //     Trae exactamente el mismo formato que el listado,
    //     pero filtrado por id_usuario.
    // ============================================================
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
        LEFT JOIN seguridad.roles r ON r.id_rol = u.id_rol
        WHERE u.id_usuario = :idUsuario
        """,
            nativeQuery = true)
    Map<String, Object> buscarDtoPorId(Integer idUsuario);

}
