package co.assip.erp.seguridad.usuariosroles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a las asignaciones de roles de los usuarios.
 *
 * Permite consultar tanto las asignaciones vigentes como
 * el histórico de roles que ha tenido cada usuario.
 */
@Repository
public interface UsuarioRolRepository
        extends JpaRepository<UsuarioRol, Long> {

    /**
     * Obtiene todas las asignaciones de un usuario,
     * incluyendo las históricas.
     */
    List<UsuarioRol> findByUsuario_IdUsuarioOrderByFechaDesdeDesc(
            Integer idUsuario
    );

    /**
     * Obtiene las asignaciones marcadas como activas
     * para un usuario.
     */
    List<UsuarioRol> findByUsuario_IdUsuarioAndActivoTrueOrderByFechaDesdeAsc(
            Integer idUsuario
    );

    /**
     * Busca una asignación activa concreta entre
     * un usuario y un rol.
     */
    Optional<UsuarioRol> findFirstByUsuario_IdUsuarioAndRol_IdRolAndActivoTrue(
            Integer idUsuario,
            Integer idRol
    );

    /**
     * Determina si existe actualmente una asignación activa
     * entre el usuario y el rol indicado.
     */
    boolean existsByUsuario_IdUsuarioAndRol_IdRolAndActivoTrue(
            Integer idUsuario,
            Integer idRol
    );

    /**
     * Obtiene los roles vigentes del usuario para una fecha determinada.
     *
     * Una asignación es vigente cuando:
     * - está activa;
     * - fecha_desde ya inició;
     * - fecha_hasta no existe o todavía no ha finalizado.
     */
    List<UsuarioRol>
    findByUsuario_IdUsuarioAndActivoTrueAndFechaDesdeLessThanEqualAndFechaHastaIsNullOrUsuario_IdUsuarioAndActivoTrueAndFechaDesdeLessThanEqualAndFechaHastaGreaterThanEqual(
            Integer idUsuario1,
            LocalDateTime fechaDesde1,
            Integer idUsuario2,
            LocalDateTime fechaDesde2,
            LocalDateTime fechaHasta
    );
}