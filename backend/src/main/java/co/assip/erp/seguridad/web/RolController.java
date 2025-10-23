package co.assip.erp.seguridad.web;

import co.assip.erp.seguridad.domain.Rol;
import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.service.RolService;
import co.assip.erp.seguridad.service.UsuarioService;
import co.assip.erp.seguridad.service.AccessValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para la gestión de roles.
 * Permite administrar los roles de los usuarios del sistema.
 */
@RestController
@RequestMapping("/seguridad/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;
    private final UsuarioService usuarioService;
    private final AccessValidator accessValidator;

    /**
     * Listar todos los roles.
     */
    @GetMapping
    public ResponseEntity<List<Rol>> listar(HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "ROLES_VIEW");
        return ResponseEntity.ok(rolService.listar());
    }

    /**
     * Buscar un rol por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Optional<Rol>> buscar(@PathVariable Integer id, HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "ROLES_VIEW");
        return ResponseEntity.ok(rolService.buscarPorId(id));
    }

    /**
     * Crear o actualizar un rol.
     */
    @PostMapping
    public ResponseEntity<Rol> guardar(@RequestBody Rol rol, HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "ROLES_EDIT");
        return ResponseEntity.ok(rolService.guardar(rol));
    }

    /**
     * Eliminar un rol por ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id, HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "ROLES_EDIT");
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 🔹 Endpoint de prueba rápida (verifica que el token sea válido).
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping(HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "ROLES_VIEW");
        return ResponseEntity.ok("✅ Token válido — Roles OK");
    }
}
