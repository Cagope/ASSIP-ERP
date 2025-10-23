package co.assip.erp.seguridad.web;

import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.service.UsuarioService;
import co.assip.erp.seguridad.service.LogEventoService;
import co.assip.erp.seguridad.service.AccessValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/seguridad/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final LogEventoService logEventoService; // ✅ Auditoría
    private final AccessValidator accessValidator;   // ✅ Validación de permisos

    // ✅ Listar todos los usuarios
    @GetMapping
    public ResponseEntity<List<Usuario>> listar(HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "USUARIOS_VIEW");
        return ResponseEntity.ok(usuarioService.listar());
    }

    // ✅ Buscar usuario por username
    @GetMapping("/{username}")
    public ResponseEntity<Optional<Usuario>> buscar(@PathVariable String username, HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "USUARIOS_VIEW");
        return ResponseEntity.ok(usuarioService.buscarPorUsername(username));
    }

    // ✅ Crear o actualizar usuario
    @PostMapping
    public ResponseEntity<Usuario> guardar(@RequestBody Usuario usuario, HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "USUARIOS_EDIT");

        Usuario guardado = usuarioService.guardar(usuario);

        // 🟡 Determinar tipo de acción
        String accion = (usuario.getIdUsuario() == null) ? "CREAR_USUARIO" : "ACTUALIZAR_USUARIO";
        String descripcion = (accion.equals("CREAR_USUARIO"))
                ? "Se creó el usuario " + guardado.getUsername()
                : "Se actualizó el usuario " + guardado.getUsername();

        // 🧩 Registrar evento
        logEventoService.registrarEvento(
                guardado.getIdUsuario(),
                "SEGURIDAD",
                accion,
                descripcion,
                req
        );

        return ResponseEntity.ok(guardado);
    }

    // ✅ Eliminar usuario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id, HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "USUARIOS_EDIT");

        usuarioService.eliminar(id);

        // 🔴 Registrar eliminación
        logEventoService.registrarEvento(
                id,
                "SEGURIDAD",
                "ELIMINAR_USUARIO",
                "Se eliminó el usuario con id " + id,
                req
        );

        return ResponseEntity.noContent().build();
    }

    // 🔹 Endpoint de prueba rápida
    @GetMapping("/ping")
    public ResponseEntity<String> ping(HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "USUARIOS_VIEW");
        return ResponseEntity.ok("✅ Token válido y acceso autorizado");
    }
}
