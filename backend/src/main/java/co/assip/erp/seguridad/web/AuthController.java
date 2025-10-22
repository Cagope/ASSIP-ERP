package co.assip.erp.seguridad.web;

import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.service.AuthService;
import co.assip.erp.seguridad.service.LogEventoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de autenticación principal del módulo de seguridad.
 * Maneja el inicio de sesión (login) y el registro de nuevos usuarios (register).
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LogEventoService logEventoService; // ✅ inyectamos el servicio de logs

    /**
     * Endpoint de inicio de sesión.
     * Recibe credenciales (username y password), valida y devuelve un token JWT.
     *
     * @param request Usuario con username y password
     * @return JSON con el token generado
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario request, HttpServletRequest httpRequest) {
        // 🔹 Autentica y obtiene usuario + token
        Map<String, Object> resultado = authService.autenticarConUsuario(
                request.getUsername(),
                request.getPassword()
        );

        Usuario usuario = (Usuario) resultado.get("usuario");
        String token = (String) resultado.get("token");

        // 🔹 Registrar el evento de inicio de sesión
        logEventoService.registrarLogin(usuario.getIdUsuario(), httpRequest);

        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * Endpoint de registro de usuario.
     * Permite crear un nuevo usuario con contraseña encriptada.
     *
     * @param request Datos del nuevo usuario
     * @return Usuario creado
     */
    @PostMapping("/register")
    public ResponseEntity<Usuario> register(@RequestBody Usuario request) {
        Usuario nuevoUsuario = authService.registrar(request);
        return ResponseEntity.ok(nuevoUsuario);
    }
}
