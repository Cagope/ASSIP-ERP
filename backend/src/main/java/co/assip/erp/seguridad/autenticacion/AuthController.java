package co.assip.erp.seguridad.autenticacion;

import co.assip.erp.seguridad.auditoria.LogEventoService;
import co.assip.erp.seguridad.permisos.Permiso;
import co.assip.erp.seguridad.roles.Rol;
import co.assip.erp.seguridad.roles.RolRepository;
import co.assip.erp.seguridad.rolespermisos.RolPermisoService;
import co.assip.erp.seguridad.usuariosagencias.UsuarioAgenciaService;
import co.assip.erp.seguridad.usuarios.Usuario;
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
 * Administra el inicio de sesión y la consulta de la sesión
 * autenticada.
 *
 * La creación y administración de usuarios se realiza
 * exclusivamente mediante el proceso de usuarios.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LogEventoService logEventoService;
    private final JwtService jwtService;
    private final UsuarioAgenciaService usuarioAgenciaService;
    private final RolRepository rolRepository;
    private final RolPermisoService rolPermisoService;

    /**
     * Autentica al usuario y entrega el token JWT.
     *
     * El estado de seguridad de la cuenta, los intentos fallidos
     * y el último acceso son gestionados por AuthService.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Usuario request,
            HttpServletRequest httpRequest
    ) {

        try {

            String ip = obtenerIpOrigen(httpRequest);

            Map<String, Object> resultado =
                    authService.autenticarConUsuario(
                            request.getUsername(),
                            request.getPassword(),
                            ip
                    );

            Integer idUsuario =
                    (Integer) resultado.get("idUsuario");

            String username =
                    (String) resultado.get("username");

            Integer idRol =
                    (Integer) resultado.get("idRol");

            String token =
                    (String) resultado.get("token");

            String nombreRol = null;

            if (idRol != null) {
                nombreRol = rolRepository
                        .findById(idRol)
                        .map(Rol::getNombreRol)
                        .orElse(null);
            }

            /*
             * Auditoría del inicio de sesión exitoso.
             */
            logEventoService.registrarLogin(
                    idUsuario,
                    httpRequest
            );

            Map<String, Object> respuesta =
                    new HashMap<>();

            respuesta.put(
                    "token",
                    token
            );

            respuesta.put(
                    "username",
                    username
            );

            respuesta.put(
                    "rol",
                    nombreRol
            );

            return ResponseEntity.ok(respuesta);

        } catch (RuntimeException ex) {

            Map<String, Object> error =
                    new HashMap<>();

            error.put(
                    "error",
                    ex.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(error);
        }
    }

    /**
     * Obtiene la información funcional de la sesión autenticada.
     *
     * Devuelve:
     * - identificador del usuario
     * - username
     * - rol
     * - agencias autorizadas
     * - permisos efectivos del rol
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> obtenerUsuarioActual(
            HttpServletRequest request
    ) {

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            Map<String, Object> error =
                    new HashMap<>();

            error.put(
                    "error",
                    "Token no proporcionado"
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(error);
        }

        try {

            String token =
                    authHeader.substring(7);

            /*
             * Valida firma y expiración antes de utilizar
             * la información contenida en el token.
             */
            if (!jwtService.validarToken(token)) {

                Map<String, Object> error =
                        new HashMap<>();

                error.put(
                        "error",
                        "Token inválido o expirado"
                );

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(error);
            }

            String username =
                    jwtService.extraerUsername(token);

            Usuario usuario =
                    authService.buscarUsuarioPorUsername(
                            username
                    );

            if (usuario == null) {

                Map<String, Object> error =
                        new HashMap<>();

                error.put(
                        "error",
                        "Usuario no encontrado"
                );

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(error);
            }

            /*
             * Un usuario desactivado o bloqueado después
             * de haber obtenido el token tampoco puede
             * continuar utilizando la sesión.
             */
            if (!Boolean.TRUE.equals(usuario.getActivo())
                    || Boolean.TRUE.equals(usuario.getBloqueado())) {

                Map<String, Object> error =
                        new HashMap<>();

                error.put(
                        "error",
                        "Usuario inactivo o bloqueado"
                );

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(error);
            }

            String nombreRol = null;

            if (usuario.getIdRol() != null) {

                nombreRol = rolRepository
                        .findById(usuario.getIdRol())
                        .map(Rol::getNombreRol)
                        .orElse(null);
            }

            /*
             * Las agencias se consultan nuevamente en base
             * de datos para obtener la autorización vigente.
             */
            var agencias =
                    usuarioAgenciaService
                            .listarAgenciasDelUsuario(
                                    usuario.getIdUsuario()
                            );

            /*
             * Los permisos efectivos provienen exclusivamente
             * de seguridad.rol_permisos.
             */
            List<String> permisos =
                    List.of();

            if (usuario.getIdRol() != null) {

                permisos = rolPermisoService
                        .listarPermisos(
                                usuario.getIdRol()
                        )
                        .stream()
                        .filter(permiso ->
                                Boolean.TRUE.equals(
                                        permiso.getActivo()
                                )
                        )
                        .map(Permiso::getCodigo)
                        .toList();
            }

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
                    "rol",
                    nombreRol
            );

            respuesta.put(
                    "agencias",
                    agencias
            );

            respuesta.put(
                    "permisos",
                    permisos
            );

            return ResponseEntity.ok(respuesta);

        } catch (Exception ex) {

            Map<String, Object> error =
                    new HashMap<>();

            error.put(
                    "error",
                    "Token inválido o expirado"
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(error);
        }
    }

    /**
     * Obtiene la dirección IP de origen de la petición.
     *
     * Si existe X-Forwarded-For utiliza la primera dirección
     * informada; de lo contrario utiliza la dirección remota
     * de la conexión.
     */
    private String obtenerIpOrigen(
            HttpServletRequest request
    ) {

        String forwardedFor =
                request.getHeader("X-Forwarded-For");

        if (forwardedFor != null
                && !forwardedFor.isBlank()) {

            return forwardedFor
                    .split(",")[0]
                    .trim();
        }

        return request.getRemoteAddr();
    }
}