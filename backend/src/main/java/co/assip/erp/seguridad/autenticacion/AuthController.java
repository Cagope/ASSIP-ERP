package co.assip.erp.seguridad.autenticacion;

import co.assip.erp.seguridad.auditoria.LogEventoService;
import co.assip.erp.seguridad.rolespermisos.dto.RolPermisoDTO;
import co.assip.erp.seguridad.rolespermisos.RolPermisoService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuariosagencias.UsuarioAgenciaService;
import co.assip.erp.seguridad.usuariosroles.UsuarioRol;
import co.assip.erp.seguridad.usuariosroles.UsuarioRolService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API REST de autenticación del ERP.
 *
 * Administra:
 * - consulta del estado de acceso;
 * - activación inicial de cuentas;
 * - recuperación de contraseñas mediante código de seguridad;
 * - inicio de sesión;
 * - cambio de contraseña del usuario autenticado;
 * - consulta del contexto funcional vigente.
 *
 * Los roles efectivos se obtienen desde usuarios_roles
 * y los permisos desde rol_permisos.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordService passwordService;
    private final LogEventoService logEventoService;
    private final JwtService jwtService;
    private final UsuarioAgenciaService usuarioAgenciaService;
    private final UsuarioRolService usuarioRolService;
    private final RolPermisoService rolPermisoService;
    private final UsuarioSesionService usuarioSesionService;

    /**
     * Consulta el estado de acceso de una cuenta.
     *
     * Este endpoint se utiliza después de que el usuario
     * digita su username y antes de solicitar contraseña.
     *
     * No autentica, no genera JWT y no modifica la cuenta.
     */
    @PostMapping("/estado-acceso")
    public ResponseEntity<Map<String, Object>> consultarEstadoAcceso(
            @RequestBody Map<String, String> request
    ) {

        try {

            String username =
                    request.get(
                            "username"
                    );

            Map<String, Object> respuesta =
                    authService.consultarEstadoAcceso(
                            username
                    );

            return ResponseEntity.ok(
                    respuesta
            );

        } catch (IllegalArgumentException ex) {

            return respuestaError(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage()
            );

        } catch (RuntimeException ex) {

            return respuestaError(
                    HttpStatus.UNAUTHORIZED,
                    ex.getMessage()
            );
        }
    }

    /**
     * Activa inicialmente una cuenta mediante el código
     * de seguridad enviado al propietario.
     *
     * La activación:
     * - valida el código;
     * - establece la contraseña definitiva;
     * - consume el código;
     * - no genera JWT.
     *
     * Después de activarse, el usuario debe iniciar sesión
     * normalmente con su nueva contraseña.
     */
    @PostMapping("/activar")
    public ResponseEntity<Map<String, Object>> activarCuenta(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest
    ) {

        try {

            String username =
                    request.get(
                            "username"
                    );

            String codigo =
                    request.get(
                            "codigo"
                    );

            String passwordNueva =
                    request.get(
                            "passwordNueva"
                    );

            Usuario usuario =
                    authService.activarCuenta(
                            username,
                            codigo,
                            passwordNueva
                    );

            /*
             * Nunca se registra el código ni la contraseña.
             */
            logEventoService.registrarEvento(
                    usuario.getIdUsuario(),
                    "SEGURIDAD",
                    "ACTIVAR_CUENTA",
                    "El usuario activó correctamente su cuenta.",
                    httpRequest
            );

            Map<String, Object> respuesta =
                    new HashMap<>();

            respuesta.put(
                    "mensaje",
                    "Cuenta activada correctamente."
            );

            respuesta.put(
                    "username",
                    usuario.getUsername()
            );

            return ResponseEntity.ok(
                    respuesta
            );

        } catch (IllegalArgumentException ex) {

            return respuestaError(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage()
            );

        } catch (RuntimeException ex) {

            return respuestaError(
                    HttpStatus.UNAUTHORIZED,
                    ex.getMessage()
            );
        }
    }

    /**
     * Restablece la contraseña de una cuenta existente
     * mediante un código de recuperación.
     *
     * La recuperación:
     * - valida el código;
     * - establece una nueva contraseña;
     * - consume el código;
     * - no genera JWT.
     *
     * Después del restablecimiento, el usuario debe iniciar
     * sesión normalmente con su nueva contraseña.
     */
    @PostMapping("/recuperar-password")
    public ResponseEntity<Map<String, Object>> recuperarPassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest
    ) {

        try {

            String username =
                    request.get(
                            "username"
                    );

            String codigo =
                    request.get(
                            "codigo"
                    );

            String passwordNueva =
                    request.get(
                            "passwordNueva"
                    );

            Usuario usuario =
                    authService.recuperarPassword(
                            username,
                            codigo,
                            passwordNueva
                    );

            /*
             * Nunca se registra el código ni la contraseña.
             */
            logEventoService.registrarEvento(
                    usuario.getIdUsuario(),
                    "SEGURIDAD",
                    "RECUPERAR_PASSWORD",
                    "El usuario restableció correctamente su contraseña mediante código de recuperación.",
                    httpRequest
            );

            Map<String, Object> respuesta =
                    new HashMap<>();

            respuesta.put(
                    "mensaje",
                    "Contraseña restablecida correctamente."
            );

            respuesta.put(
                    "username",
                    usuario.getUsername()
            );

            return ResponseEntity.ok(
                    respuesta
            );

        } catch (IllegalArgumentException ex) {

            return respuestaError(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage()
            );

        } catch (RuntimeException ex) {

            return respuestaError(
                    HttpStatus.UNAUTHORIZED,
                    ex.getMessage()
            );
        }
    }

    /**
     * Autentica al usuario mediante su contraseña definitiva
     * y entrega el JWT de sesión.
     *
     * Las cuentas pendientes de activación no pueden utilizar
     * este endpoint hasta establecer su contraseña.
     *
     * La respuesta conserva la información de vigencia
     * calculada por AuthService para que el frontend pueda
     * mostrar los avisos de vencimiento correspondientes.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Usuario request,
            HttpServletRequest httpRequest
    ) {

        try {

            String ip =
                    obtenerIpOrigen(
                            httpRequest
                    );

            Map<String, Object> resultado =
                    authService.autenticarConUsuario(
                            request.getUsername(),
                            request.getPassword(),
                            ip
                    );

            Integer idUsuario =
                    (Integer) resultado.get(
                            "idUsuario"
                    );

            /*
             * Registra la auditoría del ingreso exitoso.
             *
             * AuthService solamente retorna resultado cuando
             * la autenticación fue completada correctamente.
             */
            logEventoService.registrarLogin(
                    idUsuario,
                    httpRequest
            );

            /*
             * No reconstruimos la respuesta.
             *
             * AuthService contiene el resultado completo del
             * proceso de autenticación, incluida la información
             * de vigencia de contraseña.
             */
            return ResponseEntity.ok(
                    resultado
            );

        } catch (RuntimeException ex) {

            return respuestaError(
                    HttpStatus.UNAUTHORIZED,
                    ex.getMessage()
            );
        }
    }

    /**
     * Cambia voluntariamente la contraseña
     * del usuario autenticado.
     *
     * Requiere conocer la contraseña actual.
     *
     * No participa en la activación inicial
     * ni en la recuperación de contraseñas.
     */
    @PostMapping("/cambiar-password")
    public ResponseEntity<Map<String, Object>> cambiarPassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest
    ) {

        try {

            Integer idUsuario =
                    usuarioSesionService.idUsuario();

            String passwordActual =
                    request.get("passwordActual");

            String passwordNueva =
                    request.get("passwordNueva");

            // =====================================================
            // CAMBIO DE CONTRASEÑA
            // =====================================================

            passwordService.cambiarPassword(
                    idUsuario,
                    passwordActual,
                    passwordNueva
            );

            // =====================================================
            // AUDITORÍA
            // =====================================================

            logEventoService.registrarEvento(
                    idUsuario,
                    "SEGURIDAD",
                    "CAMBIAR_PASSWORD",
                    "El usuario cambió voluntariamente su contraseña.",
                    httpRequest
            );

            // =====================================================
            // RESPUESTA
            // =====================================================

            Map<String, Object> respuesta =
                    new HashMap<>();

            respuesta.put(
                    "mensaje",
                    "Contraseña actualizada correctamente."
            );

            return ResponseEntity.ok(
                    respuesta
            );

        } catch (IllegalArgumentException ex) {

            return respuestaError(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage()
            );

        } catch (RuntimeException ex) {

            return respuestaError(
                    HttpStatus.UNAUTHORIZED,
                    ex.getMessage()
            );
        }
    }

    /**
     * Obtiene el contexto funcional vigente
     * del usuario autenticado.
     *
     * Devuelve:
     * - identificador del usuario;
     * - username;
     * - roles vigentes;
     * - agencias autorizadas;
     * - unión de permisos de todos sus roles vigentes.
     *
     * Esta información se consulta nuevamente en base de datos
     * para no depender de roles o permisos almacenados
     * dentro de un JWT previamente emitido.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> obtenerUsuarioActual(
            HttpServletRequest request
    ) {

        String authHeader =
                request.getHeader(
                        "Authorization"
                );

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            return respuestaError(
                    HttpStatus.UNAUTHORIZED,
                    "Token no proporcionado"
            );
        }

        try {

            String token =
                    authHeader.substring(7);

            if (!jwtService.validarToken(
                    token
            )) {

                return respuestaError(
                        HttpStatus.UNAUTHORIZED,
                        "Token inválido o expirado"
                );
            }

            String username =
                    jwtService.extraerUsername(
                            token
                    );

            Usuario usuario =
                    authService.buscarUsuarioPorUsername(
                            username
                    );

            if (usuario == null) {

                return respuestaError(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                );
            }

            if (!Boolean.TRUE.equals(
                    usuario.getActivo()
            ) || Boolean.TRUE.equals(
                    usuario.getBloqueado()
            )) {

                return respuestaError(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario inactivo o bloqueado"
                );
            }

            List<UsuarioRol> asignaciones =
                    usuarioRolService.listarVigentes(
                            usuario.getIdUsuario()
                    );

            if (asignaciones.isEmpty()) {

                return respuestaError(
                        HttpStatus.UNAUTHORIZED,
                        "El usuario no tiene roles vigentes asignados"
                );
            }

            /*
             * Roles vigentes.
             */
            List<Map<String, Object>> roles =
                    asignaciones.stream()
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

            /*
             * Agencias autorizadas.
             */
            List<Map<String, Object>> agencias =
                    usuarioAgenciaService
                            .listarAgenciasDelUsuario(
                                    usuario.getIdUsuario()
                            );

            /*
             * Unión de permisos activos de todos
             * los roles vigentes.
             */
            List<String> permisos =
                    asignaciones.stream()
                            .flatMap(usuarioRol ->
                                    rolPermisoService
                                            .listarPermisos(
                                                    usuarioRol
                                                            .getRol()
                                                            .getIdRol()
                                            )
                                            .stream()
                            )
                            .filter(permiso ->
                                    Boolean.TRUE.equals(
                                            permiso.activo()
                                    )
                            )
                            .map(
                                    RolPermisoDTO::codigo
                            )
                            .filter(codigo ->
                                    codigo != null
                                            && !codigo.isBlank()
                            )
                            .map(
                                    String::trim
                            )
                            .map(
                                    String::toUpperCase
                            )
                            .distinct()
                            .sorted()
                            .toList();

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
                    "roles",
                    roles
            );

            respuesta.put(
                    "agencias",
                    agencias
            );

            respuesta.put(
                    "permisos",
                    permisos
            );

            return ResponseEntity.ok(
                    respuesta
            );

        } catch (Exception ex) {

            return respuestaError(
                    HttpStatus.UNAUTHORIZED,
                    "Token inválido o expirado"
            );
        }
    }

    /**
     * Construye una respuesta uniforme
     * para errores de autenticación.
     */
    private ResponseEntity<Map<String, Object>> respuestaError(
            HttpStatus estado,
            String mensaje
    ) {

        Map<String, Object> error =
                new HashMap<>();

        error.put(
                "error",
                mensaje
        );

        return ResponseEntity
                .status(estado)
                .body(error);
    }

    /**
     * Obtiene la dirección IP de origen.
     *
     * Cuando existe X-Forwarded-For utiliza
     * la primera dirección informada.
     */
    private String obtenerIpOrigen(
            HttpServletRequest request
    ) {

        String forwardedFor =
                request.getHeader(
                        "X-Forwarded-For"
                );

        if (forwardedFor != null
                && !forwardedFor.isBlank()) {

            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        return request.getRemoteAddr();
    }
}