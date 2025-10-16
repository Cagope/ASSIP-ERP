package co.assip.erp.seguridad.web;

import co.assip.erp.seguridad.dto.UsuarioCreateRequest;
import co.assip.erp.seguridad.dto.UsuarioEstadoRequest;
import co.assip.erp.seguridad.dto.UsuarioResetPasswordRequest;
import co.assip.erp.seguridad.dto.UsuarioResponse;
import co.assip.erp.seguridad.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seguridad/usuarios")
public class UsuariosWriteController {

    private final UsuarioService usuarioService;

    public UsuariosWriteController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ------------------------
    // Crear usuario (POST)
    // ------------------------
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody UsuarioCreateRequest body, HttpServletRequest request) {
        String actor = "admin"; // temporal hasta tener login/JWT
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");

        try {
            UsuarioResponse dto = usuarioService.crear(body, actor, ip, ua);
            return ResponseEntity.status(201).body(new SingleResponse<>(dto));
        } catch (IllegalArgumentException e) {
            // validaciones 400
            return ResponseEntity.badRequest().body(err(e.getMessage()));
        } catch (IllegalStateException e) {
            // duplicado 409
            if ("DUPLICATE_USERNAME".equals(e.getMessage())) {
                return ResponseEntity.status(409).body(err(e.getMessage()));
            }
            return ResponseEntity.status(409).body(err("CONFLICT"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(err("ERROR"));
        }
    }

    // ------------------------
    // Cambiar estado (PATCH)
    // ------------------------
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
                                           @RequestBody UsuarioEstadoRequest body,
                                           HttpServletRequest request) {
        String actor = "admin";
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");

        if (body == null || body.getActivo() == null) {
            return ResponseEntity.badRequest().body(err("INVALID_BODY"));
        }
        try {
            var dto = usuarioService.cambiarEstado(id, body.getActivo(), actor, ip, ua);
            return ResponseEntity.ok(new SingleResponse<>(dto));
        } catch (RuntimeException ex) {
            if ("NOT_FOUND".equals(ex.getMessage())) {
                return ResponseEntity.status(404).body(err("NOT_FOUND"));
            }
            return ResponseEntity.internalServerError().body(err("ERROR"));
        }
    }

    // ------------------------
    // Reset de contraseña (POST)
    // ------------------------
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id,
                                           @RequestBody(required = false) UsuarioResetPasswordRequest body,
                                           HttpServletRequest request) {
        String actor = "admin";
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");

        String nuevaPassword = (body == null) ? null : body.getNuevaPassword();
        try {
            usuarioService.resetPassword(id, nuevaPassword, actor, ip, ua);
            return ResponseEntity.ok(new MetaResponse(true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(err(e.getMessage()));
        } catch (RuntimeException ex) {
            if ("NOT_FOUND".equals(ex.getMessage())) {
                return ResponseEntity.status(404).body(err("NOT_FOUND"));
            }
            return ResponseEntity.internalServerError().body(err("ERROR"));
        }
    }

    // ------------------------
    // Forzar cambio de contraseña (POST)
    // ------------------------
    @PostMapping("/{id}/force-password-change")
    public ResponseEntity<?> forcePwChange(@PathVariable Long id, HttpServletRequest request) {
        String actor = "admin";
        String ip = request.getRemoteAddr();
        String ua = request.getHeader("User-Agent");

        try {
            usuarioService.forcePasswordChange(id, actor, ip, ua);
            return ResponseEntity.ok(new MetaResponse(true));
        } catch (RuntimeException ex) {
            if ("NOT_FOUND".equals(ex.getMessage())) {
                return ResponseEntity.status(404).body(err("NOT_FOUND"));
            }
            return ResponseEntity.internalServerError().body(err("ERROR"));
        }
    }

    // ------------------------
    // Helpers de respuesta
    // ------------------------
    record SingleResponse<T>(T data) {}
    record ErrorItem(String code, String message) {}
    record ErrorResponse(java.util.List<ErrorItem> errors) {}
    record MetaResponse(boolean ok) {}

    private ErrorResponse err(String code) {
        return new ErrorResponse(java.util.List.of(new ErrorItem(code, code)));
    }
}
