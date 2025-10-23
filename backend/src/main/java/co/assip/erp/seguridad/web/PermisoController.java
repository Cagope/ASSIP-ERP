package co.assip.erp.seguridad.web;

import co.assip.erp.seguridad.domain.Permiso;
import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.service.PermisoService;
import co.assip.erp.seguridad.service.UsuarioService;
import co.assip.erp.seguridad.service.AccessValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/seguridad/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final PermisoService permisoService;
    private final UsuarioService usuarioService;
    private final AccessValidator accessValidator;

    @GetMapping
    public ResponseEntity<List<Permiso>> listar(HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "PERMISOS_VIEW");
        return ResponseEntity.ok(permisoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Permiso>> buscar(@PathVariable Integer id, HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "PERMISOS_VIEW");
        return ResponseEntity.ok(permisoService.buscarPorId(id));
    }

    @GetMapping("/rol/{idRol}")
    public ResponseEntity<List<Permiso>> listarPorRol(@PathVariable Integer idRol, HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "ROLES_VIEW");
        return ResponseEntity.ok(permisoService.listarPorRol(idRol));
    }

    @PostMapping
    public ResponseEntity<Permiso> guardar(@RequestBody Permiso permiso, HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "PERMISOS_EDIT");
        return ResponseEntity.ok(permisoService.guardar(permiso));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id, HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "PERMISOS_EDIT");
        permisoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping(HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "PERMISOS_VIEW");
        return ResponseEntity.ok("✅ Token válido — Permisos OK");
    }
}
