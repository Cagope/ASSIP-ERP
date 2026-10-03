package co.assip.erp.seguridad.usuariosroles;

import co.assip.erp.seguridad.roles.Rol;
import co.assip.erp.seguridad.roles.RolRepository;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio encargado de administrar las asignaciones
 * de roles a los usuarios del ERP.
 *
 * Un usuario puede tener múltiples roles simultáneamente.
 * Las asignaciones conservan su periodo de vigencia y su
 * histórico cuando son retiradas.
 */
@Service
@RequiredArgsConstructor
public class UsuarioRolService {

    private final UsuarioRolRepository usuarioRolRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    /**
     * Obtiene el historial completo de asignaciones
     * de roles de un usuario.
     */
    @Transactional(readOnly = true)
    public List<UsuarioRol> listarHistorial(
            Integer idUsuario
    ) {

        validarUsuarioExiste(idUsuario);

        return usuarioRolRepository
                .findByUsuario_IdUsuarioOrderByFechaDesdeDesc(
                        idUsuario
                );
    }

    /**
     * Obtiene las asignaciones marcadas como activas.
     *
     * Este método no evalúa las fechas de vigencia.
     */
    @Transactional(readOnly = true)
    public List<UsuarioRol> listarActivos(
            Integer idUsuario
    ) {

        validarUsuarioExiste(idUsuario);

        return usuarioRolRepository
                .findByUsuario_IdUsuarioAndActivoTrueOrderByFechaDesdeAsc(
                        idUsuario
                );
    }

    /**
     * Obtiene exclusivamente los roles activos y vigentes
     * del usuario en la fecha y hora actuales.
     *
     * Una asignación es vigente cuando:
     * - está activa;
     * - fecha_desde ya inició;
     * - fecha_hasta es NULL o todavía no ha finalizado.
     */
    @Transactional(readOnly = true)
    public List<UsuarioRol> listarVigentes(
            Integer idUsuario
    ) {

        validarUsuarioExiste(idUsuario);

        LocalDateTime ahora =
                LocalDateTime.now();

        return usuarioRolRepository
                .findByUsuario_IdUsuarioAndActivoTrueOrderByFechaDesdeAsc(
                        idUsuario
                )
                .stream()
                .filter(usuarioRol ->
                        estaVigente(
                                usuarioRol,
                                ahora
                        )
                )
                .toList();
    }

    /**
     * Determina si un usuario posee actualmente
     * un rol activo y vigente.
     *
     * Este método debe utilizarse para decisiones
     * de autorización.
     */
    @Transactional(readOnly = true)
    public boolean tieneRolVigente(
            Integer idUsuario,
            Integer idRol
    ) {

        if (idUsuario == null || idRol == null) {
            return false;
        }

        LocalDateTime ahora =
                LocalDateTime.now();

        return usuarioRolRepository
                .findFirstByUsuario_IdUsuarioAndRol_IdRolAndActivoTrue(
                        idUsuario,
                        idRol
                )
                .filter(usuarioRol ->
                        estaVigente(
                                usuarioRol,
                                ahora
                        )
                )
                .isPresent();
    }

    /**
     * Determina si existe una asignación activa del rol.
     *
     * Se conserva para operaciones administrativas donde
     * interesa conocer el estado de la asignación aunque
     * todavía no haya iniciado su vigencia.
     */
    @Transactional(readOnly = true)
    public boolean tieneRolActivo(
            Integer idUsuario,
            Integer idRol
    ) {

        return usuarioRolRepository
                .existsByUsuario_IdUsuarioAndRol_IdRolAndActivoTrue(
                        idUsuario,
                        idRol
                );
    }

    /**
     * Asigna un rol a un usuario.
     *
     * La asignación inicia inmediatamente y permanece vigente
     * hasta que sea retirada.
     *
     * @param idUsuario      usuario que recibe el rol
     * @param idRol          rol que será asignado
     * @param idUsuarioActor usuario autenticado que realiza
     *                       la operación
     */
    @Transactional
    public UsuarioRol asignar(
            Integer idUsuario,
            Integer idRol,
            Integer idUsuarioActor
    ) {

        Usuario usuario =
                obtenerUsuario(idUsuario);

        Rol rol =
                obtenerRol(idRol);

        Usuario actor =
                obtenerUsuario(idUsuarioActor);

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalStateException(
                    "No se pueden asignar roles a un usuario inactivo."
            );
        }

        if (!Boolean.TRUE.equals(rol.getActivo())) {
            throw new IllegalStateException(
                    "No se puede asignar un rol inactivo."
            );
        }

        boolean yaAsignado =
                usuarioRolRepository
                        .existsByUsuario_IdUsuarioAndRol_IdRolAndActivoTrue(
                                idUsuario,
                                idRol
                        );

        if (yaAsignado) {
            throw new IllegalStateException(
                    "El usuario ya tiene activo el rol seleccionado."
            );
        }

        LocalDateTime ahora =
                LocalDateTime.now();

        UsuarioRol asignacion =
                UsuarioRol.builder()
                        .usuario(usuario)
                        .rol(rol)
                        .fechaDesde(ahora)
                        .fechaHasta(null)
                        .activo(true)
                        .usuarioCreacion(actor)
                        .fechaCreacion(ahora)
                        .build();

        return usuarioRolRepository.save(
                asignacion
        );
    }

    /**
     * Retira un rol actualmente activo del usuario.
     *
     * La asignación no se elimina físicamente.
     * Se cierra su periodo de vigencia para conservar
     * el histórico.
     */
    @Transactional
    public UsuarioRol retirar(
            Integer idUsuario,
            Integer idRol,
            Integer idUsuarioActor
    ) {

        Usuario actor =
                obtenerUsuario(idUsuarioActor);

        UsuarioRol asignacion =
                usuarioRolRepository
                        .findFirstByUsuario_IdUsuarioAndRol_IdRolAndActivoTrue(
                                idUsuario,
                                idRol
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "El usuario no tiene activo el rol seleccionado."
                                )
                        );

        LocalDateTime ahora =
                LocalDateTime.now();

        asignacion.setActivo(false);
        asignacion.setFechaHasta(ahora);
        asignacion.setUsuarioEdicion(actor);
        asignacion.setFechaEdicion(ahora);

        return usuarioRolRepository.save(
                asignacion
        );
    }

    /**
     * Determina si una asignación se encuentra vigente
     * para una fecha determinada.
     */
    private boolean estaVigente(
            UsuarioRol usuarioRol,
            LocalDateTime fecha
    ) {

        if (!Boolean.TRUE.equals(usuarioRol.getActivo())) {
            return false;
        }

        if (usuarioRol.getFechaDesde() == null
                || usuarioRol.getFechaDesde().isAfter(fecha)) {
            return false;
        }

        return usuarioRol.getFechaHasta() == null
                || !usuarioRol.getFechaHasta().isBefore(fecha);
    }

    /**
     * Obtiene un usuario o genera una excepción
     * cuando no existe.
     */
    private Usuario obtenerUsuario(
            Integer idUsuario
    ) {

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "El identificador del usuario es obligatorio."
            );
        }

        return usuarioRepository
                .findById(idUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el usuario con id "
                                        + idUsuario
                        )
                );
    }

    /**
     * Verifica la existencia de un usuario.
     */
    private void validarUsuarioExiste(
            Integer idUsuario
    ) {

        obtenerUsuario(idUsuario);
    }

    /**
     * Obtiene un rol o genera una excepción
     * cuando no existe.
     */
    private Rol obtenerRol(
            Integer idRol
    ) {

        if (idRol == null) {
            throw new IllegalArgumentException(
                    "El identificador del rol es obligatorio."
            );
        }

        return rolRepository
                .findById(idRol)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el rol con id "
                                        + idRol
                        )
                );
    }
}