package co.assip.erp.seguridad.web;

import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.service.UsuarioService;
import co.assip.erp.seguridad.service.UsuarioAgenciaService;
import co.assip.erp.seguridad.service.LogEventoService;
import co.assip.erp.seguridad.service.AccessValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/seguridad/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioAgenciaService usuarioAgenciaService;
    private final LogEventoService logEventoService;
    private final AccessValidator accessValidator;

    // ============================================================
    // LISTAR (DTO)
    // ============================================================
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listar(HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "USUARIOS_VIEW");

        return ResponseEntity.ok(usuarioService.listar());
    }

    // ============================================================
    // BUSCAR POR ID → DTO COMPLETO PARA EDICIÓN
    // ============================================================
    @GetMapping("/id/{idUsuario}")
    public ResponseEntity<Map<String, Object>> buscarPorId(
            @PathVariable Integer idUsuario,
            HttpServletRequest req
    ) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "USUARIOS_VIEW");

        Map<String, Object> dto = usuarioService.buscarDtoPorId(idUsuario);

        if (dto == null || dto.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        return ResponseEntity.ok(dto);
    }

    // ============================================================
    // BUSCAR POR USERNAME
    // ============================================================
    @GetMapping("/user/{username}")
    public ResponseEntity<Optional<Usuario>> buscar(
            @PathVariable String username,
            HttpServletRequest req
    ) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "USUARIOS_VIEW");

        return ResponseEntity.ok(usuarioService.buscarPorUsername(username));
    }

    // ============================================================
    // GUARDAR / ACTUALIZAR
    // ============================================================
    @PostMapping
    public ResponseEntity<Usuario> guardar(@RequestBody Usuario usuario, HttpServletRequest req) {
        Usuario userActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(userActual, "USUARIOS_EDIT");

        Usuario guardado = usuarioService.guardar(usuario);

        String accion = (usuario.getIdUsuario() == null) ?
                "CREAR_USUARIO" :
                "ACTUALIZAR_USUARIO";

        String descripcion = (usuario.getIdUsuario() == null ?
                "Se creó el usuario " :
                "Se actualizó el usuario ") + guardado.getUsername();

        logEventoService.registrarEvento(
                guardado.getIdUsuario(),
                "SEGURIDAD",
                accion,
                descripcion,
                req
        );

        return ResponseEntity.ok(guardado);
    }

    // ============================================================
    // ELIMINAR
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id, HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "USUARIOS_EDIT");

        usuarioService.eliminar(id);

        logEventoService.registrarEvento(
                id,
                "SEGURIDAD",
                "ELIMINAR_USUARIO",
                "Se eliminó el usuario con id " + id,
                req
        );

        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // LISTAR AGENCIAS
    // ============================================================
    @GetMapping("/{id}/agencias")
    public ResponseEntity<List<Map<String, Object>>> listarAgencias(
            @PathVariable Integer id,
            HttpServletRequest req
    ) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "USUARIOS_VIEW");

        return ResponseEntity.ok(usuarioAgenciaService.listarAgenciasDelUsuario(id));
    }

    // ============================================================
    // ASIGNAR AGENCIAS
    // ============================================================
    @PostMapping("/{id}/agencias")
    public ResponseEntity<Void> asignarAgencias(
            @PathVariable Integer id,
            @RequestBody List<Integer> agencias,
            HttpServletRequest req
    ) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "USUARIOS_EDIT");

        usuarioAgenciaService.asignarAgencias(id, agencias, usuarioActual.getIdUsuario());

        logEventoService.registrarEvento(
                id,
                "SEGURIDAD",
                "ASIGNAR_AGENCIAS",
                "Se asignaron agencias: " + agencias,
                req
        );

        return ResponseEntity.ok().build();
    }

    // ============================================================
    // PING
    // ============================================================
    @GetMapping("/ping")
    public ResponseEntity<String> ping(HttpServletRequest req) {
        Usuario usuarioActual = usuarioService.getUsuarioActual(req);
        accessValidator.validarAcceso(usuarioActual, "USUARIOS_VIEW");
        return ResponseEntity.ok("OK");
    }
}
