package co.assip.erp.seguridad.usuarios;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.auditoria.LogEventoService;
import co.assip.erp.seguridad.usuariosagencias.UsuarioAgenciaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * API REST para la administración de usuarios del sistema.
 *
 * Permite consultar, crear y actualizar usuarios, además de
 * administrar temporalmente sus agencias autorizadas.
 */
@RestController
@RequestMapping("/seguridad/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioAgenciaService usuarioAgenciaService;
    private final LogEventoService logEventoService;
    private final AccessValidator accessValidator;

    /**
     * Obtiene el listado administrativo de usuarios.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listar(
            HttpServletRequest request
    ) {

        validarAcceso(request, "USUARIOS_VIEW");

        return ResponseEntity.ok(
                usuarioService.listar()
        );
    }

    /**
     * Obtiene la información de un usuario para consulta o edición.
     */
    @GetMapping("/id/{idUsuario}")
    public ResponseEntity<Map<String, Object>> buscarPorId(
            @PathVariable Integer idUsuario,
            HttpServletRequest request
    ) {

        validarAcceso(request, "USUARIOS_VIEW");

        return ResponseEntity.ok(
                usuarioService.buscarDtoPorId(idUsuario)
        );
    }

    /**
     * Busca un usuario por su nombre de acceso.
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<Optional<Usuario>> buscarPorUsername(
            @PathVariable String username,
            HttpServletRequest request
    ) {

        validarAcceso(request, "USUARIOS_VIEW");

        return ResponseEntity.ok(
                usuarioService.buscarPorUsername(username)
        );
    }

    /**
     * Crea o actualiza un usuario.
     */
    @PostMapping
    public ResponseEntity<Usuario> guardar(
            @RequestBody Usuario usuario,
            HttpServletRequest request
    ) {

        Usuario usuarioActual =
                validarAcceso(request, "USUARIOS_EDIT");

        boolean nuevo = usuario.getIdUsuario() == null;

        /*
         * La auditoría de quién realiza la operación se establece
         * en el backend y no se confía al valor enviado por el frontend.
         */
        if (nuevo) {
            usuario.setUsuarioCreacion(
                    usuarioActual.getUsername()
            );
        } else {
            usuario.setUsuarioActualizacion(
                    usuarioActual.getUsername()
            );
        }

        Usuario guardado =
                usuarioService.guardar(usuario);

        String accion = nuevo
                ? "CREAR_USUARIO"
                : "ACTUALIZAR_USUARIO";

        String descripcion = nuevo
                ? "Se creó el usuario " + guardado.getUsername()
                : "Se actualizó el usuario " + guardado.getUsername();

        logEventoService.registrarEvento(
                guardado.getIdUsuario(),
                "SEGURIDAD",
                accion,
                descripcion,
                request
        );

        return ResponseEntity.ok(guardado);
    }

    /**
     * Obtiene las agencias autorizadas para un usuario.
     *
     * Esta operación será trasladada posteriormente al proceso
     * funcional usuariosagencias.
     */
    @GetMapping("/{id}/agencias")
    public ResponseEntity<List<Map<String, Object>>> listarAgencias(
            @PathVariable Integer id,
            HttpServletRequest request
    ) {

        validarAcceso(request, "USUARIOS_VIEW");

        return ResponseEntity.ok(
                usuarioAgenciaService.listarAgenciasDelUsuario(id)
        );
    }

    /**
     * Reemplaza las agencias autorizadas para un usuario.
     *
     * Esta operación será trasladada posteriormente al proceso
     * funcional usuariosagencias.
     */
    @PostMapping("/{id}/agencias")
    public ResponseEntity<Void> asignarAgencias(
            @PathVariable Integer id,
            @RequestBody List<Integer> agencias,
            HttpServletRequest request
    ) {

        Usuario usuarioActual =
                validarAcceso(request, "USUARIOS_EDIT");

        usuarioAgenciaService.asignarAgencias(
                id,
                agencias,
                usuarioActual.getIdUsuario()
        );

        logEventoService.registrarEvento(
                id,
                "SEGURIDAD",
                "ASIGNAR_AGENCIAS",
                "Se asignaron agencias: " + agencias,
                request
        );

        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene el usuario autenticado y valida el permiso requerido.
     */
    private Usuario validarAcceso(
            HttpServletRequest request,
            String permiso
    ) {

        Usuario usuarioActual =
                usuarioService.getUsuarioActual(request);

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );

        return usuarioActual;
    }
}