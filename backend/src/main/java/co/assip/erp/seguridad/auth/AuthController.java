// co/assip/erp/seguridad/auth/AuthController.java
package co.assip.erp.seguridad.auth;

import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import co.assip.erp.seguridad.service.PasswordService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth") //
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordService passwordService;

    public AuthController(
            AuthService authService,
            JwtService jwtService,
            UsuarioRepository usuarioRepository,
            PasswordService passwordService
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.passwordService = passwordService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body, HttpServletRequest request) {
        try {
            String ip = request.getRemoteAddr();
            String ua = request.getHeader("User-Agent");
            LoginResponse res = authService.login(body.getUsername(), body.getPassword(), ip, ua);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(err("BAD_CREDENTIALS"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(err("NO_TOKEN"));
        }
        try {
            Claims claims = jwtService.parseAndValidate(authHeader.substring(7));
            Long uid = jwtService.getUserId(claims);
            if (uid == null) return ResponseEntity.status(401).body(err("INVALID_TOKEN"));
            Usuario u = usuarioRepository.findById(uid).orElse(null);
            if (u == null) return ResponseEntity.status(401).body(err("USER_NOT_FOUND"));
            return ResponseEntity.ok(authService.me(u));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(err("INVALID_TOKEN"));
        }
    }

    // Cambiar contraseña (requiere JWT)
    @PostMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ChangePasswordRequest body
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(err("NO_TOKEN"));
        }
        try {
            Claims claims = jwtService.parseAndValidate(authHeader.substring(7));
            Long uid = jwtService.getUserId(claims);
            if (uid == null) return ResponseEntity.status(401).body(err("INVALID_TOKEN"));

            Usuario u = usuarioRepository.findById(uid).orElse(null);
            if (u == null) return ResponseEntity.status(401).body(err("USER_NOT_FOUND"));

            passwordService.cambiarPassword(uid, body.getPasswordActual(), body.getPasswordNueva());
            return ResponseEntity.noContent().build(); // 204

        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(err("INVALID_CURRENT_PASSWORD"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(err("INVALID_TOKEN"));
        }
    }

    // Utilidades de error
    record ErrorItem(String code, String message) {}
    record ErrorResponse(java.util.List<ErrorItem> errors) {}
    private ErrorResponse err(String code) {
        return new ErrorResponse(java.util.List.of(new ErrorItem(code, code)));
    }
}
