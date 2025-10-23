package co.assip.erp.seguridad.web;

import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.service.AuthService;
import co.assip.erp.seguridad.service.LogEventoService;
import co.assip.erp.seguridad.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de autenticación principal del módulo de seguridad.
 * Maneja:
 *  - /auth/login      → autenticación con JWT
 *  - /auth/register   → registro de usuarios
 *  - /auth/me         → obtención del usuario autenticado
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LogEventoService logEventoService;
    private final JwtService jwtService;

    /**
     * Endpoint de inicio de sesión.
     * Recibe credenciales (username y password), valida y devuelve un token JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Usuario request, HttpServletRequest httpRequest) {
        try {
            // 🔹 Autenticar usuario y generar token
            Map<String, Object> resultado = authService.autenticarConUsuario(
                    request.getUsername(),
                    request.getPassword()
            );

            Usuario usuario = (Usuario) resultado.get("usuario");
            String token = (String) resultado.get("token");

            // 🔹 Registrar evento de inicio de sesión
            logEventoService.registrarLogin(usuario.getIdUsuario(), httpRequest);

            // 🔹 Retornar token + datos mínimos del usuario
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", usuario.getUsername(),
                    "rol", usuario.getRol() != null ? usuario.getRol().getNombre() : null
            ));

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Registro de usuario con contraseña encriptada.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Usuario request) {
        Usuario nuevoUsuario = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "idUsuario", nuevoUsuario.getIdUsuario(),
                        "username", nuevoUsuario.getUsername(),
                        "rol", nuevoUsuario.getRol() != null ? nuevoUsuario.getRol().getNombre() : null
                ));
    }

    /**
     * ✅ Obtiene el usuario autenticado actual (según token JWT).
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> obtenerUsuarioActual(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token no proporcionado"));
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtService.extraerUsername(token);
            Usuario usuario = authService.buscarUsuarioPorUsername(username);

            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado"));
            }

            return ResponseEntity.ok(Map.of(
                    "idUsuario", usuario.getIdUsuario(),
                    "username", usuario.getUsername(),
                    "rol", usuario.getRol() != null ? usuario.getRol().getNombre() : null
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token inválido o expirado"));
        }
    }
}
