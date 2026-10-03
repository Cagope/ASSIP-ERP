package co.assip.erp.seguridad.autenticacion;

import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import co.assip.erp.seguridad.usuariosagencias.UsuarioAgenciaService;
import co.assip.erp.seguridad.usuariosroles.UsuarioRol;
import co.assip.erp.seguridad.usuariosroles.UsuarioRolService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de autenticación de usuarios de ASSIP ERP.
 *
 * Responsabilidades:
 * - consultar el estado de acceso;
 * - activar cuentas mediante códigos de seguridad;
 * - restablecer contraseñas mediante códigos de recuperación;
 * - autenticar usuarios mediante contraseña;
 * - validar la disponibilidad de las cuentas;
 * - consultar roles vigentes;
 * - evaluar la vigencia de la contraseña;
 * - generar JWT de sesión.
 *
 * La gestión de contraseñas corresponde a PasswordService.
 * La evaluación de vigencia corresponde a PasswordVigenciaService.
 * La gestión de códigos corresponde a UsuarioTokenService.
 * El registro de intentos corresponde a AuthIntentosService.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ESTADO_PENDIENTE_ACTIVACION =
            "PENDIENTE_ACTIVACION";

    private static final String ESTADO_ACTIVO =
            "ACTIVO";

    private final UsuarioRepository usuarioRepository;
    private final PasswordService passwordService;
    private final PasswordVigenciaService passwordVigenciaService;
    private final UsuarioTokenService usuarioTokenService;
    private final AuthIntentosService authIntentosService;
    private final JwtService jwtService;
    private final UsuarioAgenciaService usuarioAgenciaService;
    private final UsuarioRolService usuarioRolService;

    /**
     * Consulta el estado de acceso de una cuenta.
     *
     * No autentica al usuario ni genera JWT.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> consultarEstadoAcceso(
            String username
    ) {

        if (username == null || username.isBlank()) {

            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }

        Usuario usuario =
                usuarioRepository
                        .findByUsername(
                                username.trim()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Usuario no encontrado."
                                )
                        );

        validarCuentaDisponible(
                usuario
        );

        obtenerRolesVigentes(
                usuario
        );

        boolean pendienteActivacion =
                usuario.getPassword() == null
                        || usuario.getPassword().isBlank()
                        || Boolean.TRUE.equals(
                        usuario.getRequiereCambioPassword()
                );

        /*
         * La longitud mínima no se define en el frontend.
         *
         * Se obtiene de la política de contraseñas utilizando
         * la agencia principal del usuario y el parámetro 705.
         */
        int longitudMinimaPassword =
                passwordService
                        .obtenerLongitudMinimaPassword(
                                usuario
                        );

        Map<String, Object> respuesta =
                new HashMap<>();

        respuesta.put(
                "idUsuario",
                usuario.getIdUsuario()
        );

        respuesta.put(
                "username",
                usuario.getUsername()
        );

        respuesta.put(
                "estado",
                pendienteActivacion
                        ? ESTADO_PENDIENTE_ACTIVACION
                        : ESTADO_ACTIVO
        );

        respuesta.put(
                "longitudMinimaPassword",
                longitudMinimaPassword
        );

        return respuesta;
    }

    /**
     * Activa una cuenta mediante código de seguridad.
     *
     * Toda la activación se ejecuta en una transacción.
     *
     * El bloqueo pesimista del usuario impide que dos
     * activaciones concurrentes establezcan contraseñas
     * diferentes para la misma cuenta.
     *
     * La contraseña y el consumo del código se confirman
     * conjuntamente.
     *
     * No genera JWT.
     */
    @Transactional
    public Usuario activarCuenta(
            String username,
            String codigo,
            String passwordNueva
    ) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }

        /*
         * Primero se obtiene el identificador.
         *
         * No se mantiene una entidad Usuario sin bloqueo
         * mientras se inicia la activación.
         */
        Usuario usuarioConsulta = usuarioRepository
                .findByUsername(username.trim())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado."
                        )
                );

        /*
         * Bloqueo exclusivo de la cuenta.
         *
         * La transacción mantiene este bloqueo hasta
         * confirmar o revertir la activación.
         *
         * También coordina la activación con la emisión
         * de nuevos códigos, que bloquea la misma fila.
         */
        Usuario usuario = usuarioRepository
                .findByIdForUpdate(
                        usuarioConsulta.getIdUsuario()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado."
                        )
                );

        validarCuentaDisponible(usuario);
        obtenerRolesVigentes(usuario);

        /*
         * La activación inicial exige que la cuenta
         * todavía no tenga contraseña.
         */
        if (usuario.getPassword() != null
                && !usuario.getPassword().isBlank()) {

            throw new IllegalStateException(
                    "La cuenta ya se encuentra activada."
            );
        }

        if (!Boolean.TRUE.equals(
                usuario.getRequiereCambioPassword()
        )) {

            throw new IllegalStateException(
                    "La cuenta no se encuentra pendiente de activación."
            );
        }

        /*
         * Valida el código.
         *
         * Los intentos fallidos son gestionados
         * independientemente por UsuarioTokenIntentosService.
         */
        UsuarioToken token = usuarioTokenService
                .validarTokenActivacion(
                        usuario.getIdUsuario(),
                        codigo
                );

        /*
         * Establece la contraseña definitiva.
         *
         * Si esta operación falla, la transacción
         * no consume el código.
         */
        passwordService.establecerPasswordActivacion(
                usuario.getIdUsuario(),
                passwordNueva
        );

        /*
         * Consume el código en la misma transacción
         * que establece la contraseña.
         */
        usuarioTokenService.marcarUtilizado(
                token,
                usuario.getIdUsuario()
        );

        /*
         * La entidad administrada refleja el estado
         * actualizado de la cuenta.
         */
        return usuarioRepository
                .findById(usuario.getIdUsuario())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No fue posible recuperar el usuario activado."
                        )
                );
    }

    /**
     * Restablece la contraseña de una cuenta existente
     * mediante un código de recuperación.
     *
     * Toda la recuperación se ejecuta en una transacción.
     *
     * El bloqueo pesimista del usuario impide que dos
     * recuperaciones concurrentes modifiquen la contraseña
     * de la misma cuenta.
     *
     * La contraseña y el consumo del código se confirman
     * conjuntamente.
     *
     * No genera JWT.
     */
    @Transactional
    public Usuario recuperarPassword(
            String username,
            String codigo,
            String passwordNueva
    ) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }

        /*
         * Primero se obtiene el identificador del usuario.
         */
        Usuario usuarioConsulta = usuarioRepository
                .findByUsername(username.trim())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado."
                        )
                );

        /*
         * Bloqueo exclusivo de la cuenta.
         *
         * Coordina el restablecimiento con la emisión
         * de nuevos códigos de seguridad.
         */
        Usuario usuario = usuarioRepository
                .findByIdForUpdate(
                        usuarioConsulta.getIdUsuario()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado."
                        )
                );

        validarCuentaDisponible(usuario);
        obtenerRolesVigentes(usuario);

        /*
         * La recuperación corresponde a una cuenta
         * que ya posee una contraseña establecida.
         */
        if (usuario.getPassword() == null
                || usuario.getPassword().isBlank()) {

            throw new IllegalStateException(
                    "La cuenta se encuentra pendiente de activación."
            );
        }

        /*
         * Valida el código de recuperación.
         *
         * Los intentos fallidos son gestionados
         * independientemente por UsuarioTokenIntentosService.
         */
        UsuarioToken token = usuarioTokenService
                .validarTokenRecuperacion(
                        usuario.getIdUsuario(),
                        codigo
                );

        /*
         * Establece la nueva contraseña.
         *
         * Si esta operación falla, la transacción
         * no consume el código.
         */
        passwordService.establecerPasswordRecuperacion(
                usuario.getIdUsuario(),
                passwordNueva
        );

        /*
         * Consume el código en la misma transacción
         * que establece la nueva contraseña.
         */
        usuarioTokenService.marcarUtilizado(
                token,
                usuario.getIdUsuario()
        );

        return usuarioRepository
                .findById(usuario.getIdUsuario())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No fue posible recuperar el usuario."
                        )
                );
    }

    /**
     * Autentica mediante usuario y contraseña.
     *
     * Los intentos fallidos se registran en
     * transacciones independientes.
     *
     * La vigencia de la contraseña se evalúa solamente
     * después de comprobar que la contraseña ingresada
     * es correcta.
     */
    public Map<String, Object> autenticarConUsuario(
            String username,
            String password,
            String ip
    ) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio."
            );
        }

        Usuario usuario = usuarioRepository
                .findByUsername(username.trim())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario o contraseña incorrectos."
                        )
                );

        validarCuentaDisponible(usuario);

        if (usuario.getPassword() == null
                || usuario.getPassword().isBlank()
                || Boolean.TRUE.equals(
                usuario.getRequiereCambioPassword()
        )) {

            throw new IllegalStateException(
                    "La cuenta se encuentra pendiente de activación."
            );
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        List<UsuarioRol> rolesVigentes =
                obtenerRolesVigentes(usuario);

        /*
         * Contraseña incorrecta.
         */
        if (!passwordService.verificarPassword(
                usuario,
                password
        )) {

            authIntentosService.registrarIntentoFallido(
                    usuario.getIdUsuario()
            );

            throw new IllegalArgumentException(
                    "Usuario o contraseña incorrectos."
            );
        }

        /*
         * La contraseña ya fue comprobada.
         *
         * Ahora puede evaluarse su vigencia sin revelar
         * información de seguridad antes de validar
         * correctamente las credenciales.
         */
        PasswordVigenciaService.ResultadoVigenciaPassword
                vigenciaPassword =
                passwordVigenciaService.evaluar(
                        usuario
                );

        /*
         * Una contraseña caducada no puede iniciar sesión.
         *
         * No se registra como intento fallido porque la
         * contraseña proporcionada fue correcta.
         *
         * Tampoco se genera JWT.
         */
        if (vigenciaPassword.getEstado()
                == PasswordVigenciaService.EstadoVigenciaPassword.CADUCADA) {

            throw new IllegalStateException(
                    "La contraseña ha caducado."
            );
        }

        /*
         * Consulta nuevamente la cuenta antes
         * de registrar el acceso exitoso.
         */
        Usuario usuarioActualizado = usuarioRepository
                .findById(usuario.getIdUsuario())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Usuario no encontrado."
                        )
                );

        validarCuentaDisponible(usuarioActualizado);

        /*
         * Registra el inicio de sesión exitoso
         * y reinicia el contador.
         */
        authIntentosService.registrarLoginExitoso(
                usuarioActualizado.getIdUsuario(),
                ip
        );

        /*
         * Consulta las agencias autorizadas.
         */
        List<Integer> agencias = usuarioAgenciaService
                .obtenerIdAgencias(
                        usuarioActualizado.getIdUsuario()
                );

        /*
         * Genera el JWT.
         *
         * Los roles y permisos no se almacenan
         * dentro del token.
         */
        String token = jwtService.generarTokenConClaims(
                usuarioActualizado,
                agencias
        );

        List<Map<String, Object>> roles =
                construirRolesRespuesta(
                        rolesVigentes
                );

        Map<String, Object> respuesta =
                new HashMap<>();

        respuesta.put(
                "idUsuario",
                usuarioActualizado.getIdUsuario()
        );

        respuesta.put(
                "username",
                usuarioActualizado.getUsername()
        );

        respuesta.put(
                "roles",
                roles
        );

        respuesta.put(
                "agencias",
                agencias
        );

        respuesta.put(
                "requiereCambioPassword",
                false
        );

        /*
         * Información de vigencia para el frontend.
         *
         * VIGENTE:
         * no requiere mostrar aviso.
         *
         * PROXIMA_VENCER:
         * permite ingresar y muestra los días restantes.
         *
         * VENCE_HOY:
         * permite ingresar y advierte que la contraseña
         * vence durante el día actual.
         */
        respuesta.put(
                "estadoVigenciaPassword",
                vigenciaPassword
                        .getEstado()
                        .name()
        );

        respuesta.put(
                "diasRestantesPassword",
                vigenciaPassword
                        .getDiasRestantes()
        );

        respuesta.put(
                "fechaVencimientoPassword",
                vigenciaPassword
                        .getFechaVencimiento()
        );

        respuesta.put(
                "mostrarAvisoVencimientoPassword",
                vigenciaPassword.getEstado()
                        == PasswordVigenciaService
                        .EstadoVigenciaPassword
                        .PROXIMA_VENCER
                        || vigenciaPassword.getEstado()
                        == PasswordVigenciaService
                        .EstadoVigenciaPassword
                        .VENCE_HOY
        );

        respuesta.put(
                "token",
                token
        );

        return respuesta;
    }

    /**
     * Busca un usuario por su nombre de acceso.
     *
     * No autentica ni genera JWT.
     */
    @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorUsername(
            String username
    ) {

        if (username == null || username.isBlank()) {
            return null;
        }

        return usuarioRepository
                .findByUsername(username.trim())
                .orElse(null);
    }

    /**
     * Valida que una cuenta esté disponible.
     *
     * Esta validación corresponde al estado general
     * de la cuenta y no a la vigencia de la contraseña.
     */
    private void validarCuentaDisponible(
            Usuario usuario
    ) {

        if (!Boolean.TRUE.equals(
                usuario.getActivo()
        )) {

            throw new IllegalStateException(
                    "Usuario inactivo."
            );
        }

        LocalDateTime ahora =
                LocalDateTime.now();

        if (usuario.getFechaInicioVigencia() != null
                && usuario.getFechaInicioVigencia()
                .isAfter(ahora)) {

            throw new IllegalStateException(
                    "La cuenta todavía no se encuentra vigente."
            );
        }

        if (usuario.getFechaFinVigencia() != null
                && !usuario.getFechaFinVigencia()
                .isAfter(ahora)) {

            throw new IllegalStateException(
                    "La vigencia de la cuenta ha finalizado."
            );
        }

        if (Boolean.TRUE.equals(
                usuario.getBloqueado()
        )) {

            throw new IllegalStateException(
                    "Usuario bloqueado."
            );
        }
    }

    /**
     * Obtiene los roles activos y vigentes.
     */
    private List<UsuarioRol> obtenerRolesVigentes(
            Usuario usuario
    ) {

        List<UsuarioRol> rolesVigentes =
                usuarioRolService.listarVigentes(
                        usuario.getIdUsuario()
                );

        if (rolesVigentes.isEmpty()) {

            throw new IllegalStateException(
                    "El usuario no tiene roles vigentes asignados."
            );
        }

        return rolesVigentes;
    }

    /**
     * Construye la información de roles
     * devuelta al frontend después del login.
     */
    private List<Map<String, Object>> construirRolesRespuesta(
            List<UsuarioRol> rolesVigentes
    ) {

        return rolesVigentes.stream()
                .map(usuarioRol -> {

                    Map<String, Object> rol =
                            new HashMap<>();

                    rol.put(
                            "idRol",
                            usuarioRol
                                    .getRol()
                                    .getIdRol()
                    );

                    rol.put(
                            "nombreRol",
                            usuarioRol
                                    .getRol()
                                    .getNombreRol()
                    );

                    return rol;
                })
                .toList();
    }
}