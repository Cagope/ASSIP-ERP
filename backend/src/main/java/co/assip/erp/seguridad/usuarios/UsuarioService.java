package co.assip.erp.seguridad.usuarios;

import co.assip.erp.seguridad.usuarios.dto.UsuarioListDTO;
import co.assip.erp.seguridad.usuariosroles.UsuarioRol;
import co.assip.erp.seguridad.usuariosroles.UsuarioRolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio administrativo de usuarios del ERP.
 *
 * Su responsabilidad es administrar la cuenta de usuario:
 * identificación, datos administrativos, estado, vigencia
 * y relación básica con la persona.
 *
 * No administra contraseñas, intentos de autenticación,
 * recuperación de acceso ni tokens de seguridad.
 *
 * Los roles se administran mediante seguridad.usuarios_roles
 * y las agencias mediante seguridad.usuarios_agencias.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final UsuarioSesionService usuarioSesionService;

    /**
     * Obtiene el listado administrativo de usuarios.
     *
     * Cada usuario incluye sus roles activos y vigentes
     * registrados en seguridad.usuarios_roles.
     */
    @Transactional(readOnly = true)
    public List<UsuarioListDTO> listar() {

        return usuarioRepository
                .findAllByOrderByUsernameAsc()
                .stream()
                .map(this::construirDto)
                .toList();
    }

    /**
     * Obtiene la información administrativa de un usuario
     * para consulta o edición.
     */
    @Transactional(readOnly = true)
    public UsuarioListDTO buscarDtoPorId(Integer idUsuario) {

        Usuario usuario = usuarioRepository
                .findById(idUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el usuario con id " + idUsuario
                        )
                );

        return construirDto(usuario);
    }

    /**
     * Construye la representación administrativa segura
     * de un usuario.
     *
     * No expone contraseña, tokens ni información interna
     * de autenticación.
     */
    private UsuarioListDTO construirDto(Usuario usuario) {

        List<UsuarioListDTO.RolDTO> roles =
                usuarioRolRepository
                        .findByUsuario_IdUsuarioAndActivoTrueOrderByFechaDesdeAsc(
                                usuario.getIdUsuario()
                        )
                        .stream()
                        .filter(this::asignacionVigente)
                        .map(this::construirRolDto)
                        .toList();

        return UsuarioListDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .idDatosPersonal(usuario.getIdDatosPersonal())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .email(usuario.getEmail())
                .activo(usuario.getActivo())
                .pendienteActivacion(
                        (usuario.getPassword() == null
                                || usuario.getPassword().isBlank())
                                && Boolean.TRUE.equals(
                                usuario.getRequiereCambioPassword()
                        )
                )
                .fechaInicioVigencia(usuario.getFechaInicioVigencia())
                .fechaFinVigencia(usuario.getFechaFinVigencia())
                .roles(roles)
                .idAgenciaPrincipal(usuario.getIdAgenciaPrincipal())
                .build();
    }

    /**
     * Construye la información básica de un rol asignado.
     */
    private UsuarioListDTO.RolDTO construirRolDto(
            UsuarioRol usuarioRol
    ) {

        return UsuarioListDTO.RolDTO.builder()
                .idRol(usuarioRol.getRol().getIdRol())
                .nombreRol(usuarioRol.getRol().getNombreRol())
                .build();
    }

    /**
     * Determina si una asignación de rol se encuentra
     * activa y dentro de su periodo de vigencia.
     */
    private boolean asignacionVigente(
            UsuarioRol usuarioRol
    ) {

        LocalDateTime ahora = LocalDateTime.now();

        if (!Boolean.TRUE.equals(usuarioRol.getActivo())) {
            return false;
        }

        if (usuarioRol.getFechaDesde() != null
                && usuarioRol.getFechaDesde().isAfter(ahora)) {
            return false;
        }

        return usuarioRol.getFechaHasta() == null
                || !usuarioRol.getFechaHasta().isBefore(ahora);
    }

    /**
     * Busca un usuario por su nombre de acceso.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorUsername(
            String username
    ) {

        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        return usuarioRepository.findByUsername(
                username.trim()
        );
    }

    /**
     * Busca un usuario por su identificador.
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(
            Integer idUsuario
    ) {

        if (idUsuario == null) {
            return Optional.empty();
        }

        return usuarioRepository.findById(idUsuario);
    }

    /**
     * Crea o actualiza administrativamente una cuenta.
     *
     * No administra contraseñas, roles ni agencias.
     */
    @Transactional
    public Usuario guardar(
            Usuario usuario
    ) {

        validarDatosBasicos(usuario);

        if (usuario.getIdUsuario() == null) {
            return crear(usuario);
        }

        Usuario actual =
                usuarioRepository.findById(
                                usuario.getIdUsuario()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe el usuario con id "
                                                + usuario.getIdUsuario()
                                )
                        );

        return actualizar(
                actual,
                usuario
        );
    }

    /**
     * Actualiza exclusivamente los datos administrativos
     * de una cuenta existente.
     *
     * No modifica contraseña, roles, agencias ni controles
     * internos de autenticación.
     */
    private Usuario actualizar(
            Usuario actual,
            Usuario cambios
    ) {

        validarUsernameDisponible(
                cambios.getUsername(),
                actual.getIdUsuario()
        );

        actual.setIdDatosPersonal(
                cambios.getIdDatosPersonal()
        );

        actual.setUsername(
                cambios.getUsername().trim()
        );

        actual.setNombreCompleto(
                cambios.getNombreCompleto()
        );

        actual.setEmail(
                normalizarEmail(
                        cambios.getEmail()
                )
        );

        if (cambios.getActivo() != null) {
            actual.setActivo(
                    cambios.getActivo()
            );
        }

        actual.setIdAgenciaPrincipal(
                cambios.getIdAgenciaPrincipal()
        );

        actual.setFechaInicioVigencia(
                cambios.getFechaInicioVigencia()
        );

        actual.setFechaFinVigencia(
                cambios.getFechaFinVigencia()
        );

        LocalDateTime ahora =
                LocalDateTime.now();

        actual.setFechaActualizacion(
                ahora
        );

        actual.setFechaEdicion(
                ahora
        );

        Integer idUsuarioActor = usuarioSesionService.idUsuario();

        Usuario actor = usuarioRepository.findById(idUsuarioActor)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el usuario autenticado."
                ));

        actual.setUsuarioActualizacion(actor.getUsername());
        actual.setFkSeguridadEdicion(idUsuarioActor);

        return usuarioRepository.save(
                actual
        );
    }

    /**
     * Inicializa una nueva cuenta administrativa.
     *
     * Solo acepta los datos administrativos permitidos.
     * Los campos internos de seguridad se inicializan
     * exclusivamente desde el backend.
     *
     * La contraseña será establecida posteriormente
     * por el propietario durante la activación.
     */
    private Usuario crear(Usuario datos) {

        validarUsernameDisponible(
                datos.getUsername(),
                null
        );

        LocalDateTime ahora = LocalDateTime.now();

        // =========================================================
        // IDENTIFICACIÓN Y DATOS ADMINISTRATIVOS
        // =========================================================

        Usuario usuario = new Usuario();

        usuario.setIdDatosPersonal(
                datos.getIdDatosPersonal()
        );

        usuario.setUsername(
                datos.getUsername().trim()
        );

        usuario.setNombreCompleto(
                datos.getNombreCompleto()
        );

        usuario.setEmail(
                normalizarEmail(datos.getEmail())
        );

        usuario.setActivo(true);

        usuario.setIdAgenciaPrincipal(
                datos.getIdAgenciaPrincipal()
        );

        usuario.setFechaInicioVigencia(
                datos.getFechaInicioVigencia() != null
                        ? datos.getFechaInicioVigencia()
                        : ahora
        );

        usuario.setFechaFinVigencia(
                datos.getFechaFinVigencia()
        );

        // =========================================================
        // CONTRASEÑA Y ACTIVACIÓN
        // =========================================================

        usuario.setPassword(null);
        usuario.setRequiereCambioPassword(true);

        usuario.setPasswordIntentos(0);
        usuario.setPasswordUltimoCambio(null);
        usuario.setPasswordExpira(null);

        // =========================================================
        // BLOQUEO Y AUTENTICACIÓN
        // =========================================================

        usuario.setBloqueado(false);
        usuario.setBloqueadoHasta(null);
        usuario.setFechaBloqueo(null);

        usuario.setUltimoLogin(null);
        usuario.setUltimoIntentoLogin(null);
        usuario.setIpUltimoLogin(null);

        // =========================================================
        // CAMPOS HEREDADOS
        // =========================================================

        usuario.setTokenRecuperacion(null);
        usuario.setTokenExpira(null);

        // =========================================================
        // AUDITORÍA
        // =========================================================

        usuario.setFechaCreacion(ahora);
        usuario.setFechaActualizacion(ahora);
        usuario.setFechaEdicion(ahora);

        /*
         * La identidad del administrador responsable
         * se obtiene directamente de la sesión autenticada.
         */
        Integer idUsuarioActor = usuarioSesionService.idUsuario();

        Usuario actor = usuarioRepository.findById(idUsuarioActor)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el usuario autenticado."
                ));

        usuario.setFkSeguridadCreacion(idUsuarioActor);
        usuario.setUsuarioCreacion(actor.getUsername());

        // No se aceptan datos de auditoría de edición.
        usuario.setFkSeguridadEdicion(null);
        usuario.setUsuarioActualizacion(null);

        return usuarioRepository.save(usuario);
    }

    /**
     * Verifica que el nombre de usuario no se encuentre
     * asignado a otra cuenta.
     */
    private void validarUsernameDisponible(
            String username,
            Integer idUsuarioActual
    ) {

        String usernameNormalizado =
                username.trim();

        Optional<Usuario> existente =
                usuarioRepository.findByUsername(
                        usernameNormalizado
                );

        if (existente.isEmpty()) {
            return;
        }

        Usuario usuarioExistente =
                existente.get();

        if (idUsuarioActual == null
                || !idUsuarioActual.equals(
                usuarioExistente.getIdUsuario()
        )) {

            throw new IllegalArgumentException(
                    "El nombre de usuario ya se encuentra registrado."
            );
        }
    }

    /**
     * Valida los datos mínimos necesarios para
     * administrar una cuenta.
     *
     * Las credenciales, roles y agencias se validan
     * en sus procesos correspondientes.
     */
    private void validarDatosBasicos(
            Usuario usuario
    ) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "Los datos del usuario son obligatorios."
            );
        }

        if (usuario.getUsername() == null
                || usuario.getUsername().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio."
            );
        }

        // =========================================================
        // VALIDACIÓN DE VIGENCIA
        // =========================================================

        if (usuario.getFechaInicioVigencia() != null
                && usuario.getFechaFinVigencia() != null
                && usuario.getFechaFinVigencia().isBefore(
                usuario.getFechaInicioVigencia()
        )) {

            throw new IllegalArgumentException(
                    "La fecha de finalización de vigencia "
                            + "no puede ser anterior a la fecha de inicio."
            );
        }

    }

    /**
     * Normaliza el correo electrónico administrativo.
     */
    private String normalizarEmail(
            String email
    ) {

        if (email == null) {
            return null;
        }

        String emailNormalizado =
                email.trim();

        return emailNormalizado.isEmpty()
                ? null
                : emailNormalizado;
    }

}