package co.assip.erp.seguridad.web;

import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.domain.Rol;
import co.assip.erp.seguridad.domain.Permiso;
import co.assip.erp.seguridad.service.AuthService;
import co.assip.erp.seguridad.service.LogEventoService;
import co.assip.erp.seguridad.service.JwtService;
import co.assip.erp.seguridad.service.UsuarioAgenciaService;
import co.assip.erp.seguridad.repository.RolRepository;
import co.assip.erp.seguridad.repository.PermisoRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LogEventoService logEventoService;
    private final JwtService jwtService;
    private final UsuarioAgenciaService usuarioAgenciaService;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository; // ✅ nuevo

    // =====================================================================================
    // LOGIN
    // =====================================================================================
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Usuario request,
                                                     HttpServletRequest httpRequest) {
        try {
            Map<String, Object> resultado = authService.autenticarConUsuario(
                    request.getUsername(),
                    request.getPassword()
            );

            Integer idUsuario = (Integer) resultado.get("idUsuario");
            String username = (String) resultado.get("username");
            Integer idRol = (Integer) resultado.get("idRol");
            String token = (String) resultado.get("token");

            // Buscar nombre del rol
            String nombreRol = rolRepository.findById(idRol)
                    .map(Rol::getNombreRol)
                    .orElse(null);

            logEventoService.registrarLogin(idUsuario, httpRequest);

            Map<String, Object> resp = new HashMap<>();
            resp.put("token", token);
            resp.put("username", username);
            resp.put("rol", nombreRol);

            return ResponseEntity.ok(resp);

        } catch (RuntimeException ex) {
            ex.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("error", ex.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    // =====================================================================================
    // REGISTER  (solo se usa en pruebas)
    // =====================================================================================
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Usuario request) {

        Usuario nuevoUsuario = authService.registrar(request);

        String nombreRol = rolRepository.findById(nuevoUsuario.getIdRol())
                .map(Rol::getNombreRol)
                .orElse(null);

        Map<String, Object> resp = new HashMap<>();
        resp.put("idUsuario", nuevoUsuario.getIdUsuario());
        resp.put("username", nuevoUsuario.getUsername());
        resp.put("rol", nombreRol);

        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // =====================================================================================
    // /auth/me — Datos del usuario autenticado + PERMISOS
    // =====================================================================================
    // =====================================================================================
// /auth/me — Datos del usuario autenticado + PERMISOS
// =====================================================================================
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> obtenerUsuarioActual(HttpServletRequest request) {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Token no proporcionado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtService.extraerUsername(token);
            Usuario usuario = authService.buscarUsuarioPorUsername(username);

            if (usuario == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Rol (por idRol)
            String nombreRol = null;
            if (usuario.getIdRol() != null) {
                nombreRol = rolRepository.findById(usuario.getIdRol())
                        .map(Rol::getNombreRol)
                        .orElse(null);
            }

            // Agencias asignadas
            var agencias = usuarioAgenciaService.listarAgenciasDelUsuario(usuario.getIdUsuario());

            // PERMISOS DEL ROL
            List<String> permisos = new java.util.ArrayList<>(); // ← FIX
            if (usuario.getIdRol() != null) {
                permisos = permisoRepository.findByIdRol(usuario.getIdRol())
                        .stream()
                        .filter(p -> p.getActivo() == null || Boolean.TRUE.equals(p.getActivo()))
                        .map(Permiso::getCodigo)
                        .toList();
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("idUsuario", usuario.getIdUsuario());
            resp.put("username", usuario.getUsername());
            resp.put("rol", nombreRol);
            resp.put("agencias", agencias);
            resp.put("permisos", permisos);

            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Token inválido o expirado");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}
