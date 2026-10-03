package co.assip.erp.seguridad.usuarios;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.auditoria.LogEventoService;
import co.assip.erp.seguridad.usuarios.dto.UsuarioListDTO;
import co.assip.erp.seguridad.usuarios.dto.UsuarioAdministracionDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuariosagencias.UsuarioAgenciaService;
import co.assip.erp.seguridad.usuariosroles.UsuarioRolService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

/**
 * API REST para la administración de usuarios del sistema.
 *
 * Permite consultar, crear y actualizar cuentas de usuario,
 * administrar sus agencias autorizadas y gestionar múltiples roles.
 *
 * Las respuestas administrativas utilizan DTO y no exponen
 * contraseñas, tokens ni información interna de autenticación.
 */
@RestController
@RequestMapping("/seguridad/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioAdministracionService usuarioAdministracionService;
    private final UsuarioAgenciaService usuarioAgenciaService;
    private final UsuarioRolService usuarioRolService;
    private final LogEventoService logEventoService;
    private final AccessValidator accessValidator;
    private final UsuarioSesionService usuarioSesionService;

    /**
     * Obtiene el listado administrativo de usuarios.
     */
    @GetMapping
    public ResponseEntity<List<UsuarioListDTO>> listar(
            HttpServletRequest request
    ) {

        validarAcceso(
                request,
                "USUARIOS_VIEW"
        );

        return ResponseEntity.ok(
                usuarioService.listar()
        );
    }

    /**
     * Obtiene un usuario por su identificador.
     */
    @GetMapping("/id/{idUsuario}")
    public ResponseEntity<UsuarioListDTO> buscarPorId(
            @PathVariable Integer idUsuario,
            HttpServletRequest request
    ) {

        validarAcceso(
                request,
                "USUARIOS_VIEW"
        );

        return ResponseEntity.ok(
                usuarioService.buscarDtoPorId(
                        idUsuario
                )
        );
    }

    /**
     * Obtiene un usuario por su nombre de acceso.
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<UsuarioListDTO> buscarPorUsername(
            @PathVariable String username,
            HttpServletRequest request
    ) {

        validarAcceso(
                request,
                "USUARIOS_VIEW"
        );

        Usuario usuario =
                usuarioService
                        .buscarPorUsername(username)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe el usuario "
                                                + username
                                )
                        );

        return ResponseEntity.ok(
                usuarioService.buscarDtoPorId(
                        usuario.getIdUsuario()
                )
        );
    }

    /**
     * Crea una cuenta administrativa con sus roles
     * y agencias autorizadas en una sola transacción.
     *
     * No recibe contraseñas ni datos de auditoría.
     */
    @PostMapping("/completo")
    public ResponseEntity<UsuarioListDTO> crearCompleto(
            @RequestBody UsuarioAdministracionDTO datos,
            HttpServletRequest request
    ) {

        Usuario usuarioActual = validarAcceso(
                request,
                "USUARIOS_EDIT"
        );

        UsuarioListDTO creado =
                usuarioAdministracionService.crearCompleto(datos);

        logEventoService.registrarEvento(
                usuarioActual.getIdUsuario(),
                "SEGURIDAD",
                "CREAR_USUARIO",
                "Se creó el usuario "
                        + creado.getUsername()
                        + " con sus roles y agencias autorizadas.",
                request
        );

        return ResponseEntity.ok(creado);
    }

    /**
     * Genera y descarga el documento cifrado
     * para la gestión de contraseña del usuario.
     *
     * Permite la activación inicial o el restablecimiento
     * de contraseña de una cuenta existente.
     *
     * Requiere permiso USUARIOS_EDIT.
     */
    @PostMapping(
            value = "/{idUsuario}/documento-password",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> generarDocumentoGestionPassword(
            @PathVariable Integer idUsuario,
            HttpServletRequest request
    ) {

        Usuario usuarioActual = validarAcceso(
                request,
                "USUARIOS_EDIT"
        );

        byte[] pdf = usuarioAdministracionService
                .generarDocumentoGestionPassword(idUsuario);

        logEventoService.registrarEvento(
                usuarioActual.getIdUsuario(),
                "SEGURIDAD",
                "GENERAR_DOCUMENTO_PASSWORD",
                "Se generó el documento de gestión de contraseña del usuario "
                        + idUsuario,
                request
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"gestion_password_usuario_"
                                + idUsuario + ".pdf\""
                )
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }

    /**
     * Crea o actualiza una cuenta de usuario.
     *
     * Los roles se administran mediante los endpoints
     * específicos de roles del usuario.
     */
    @PostMapping
    public ResponseEntity<UsuarioListDTO> guardar(
            @RequestBody Usuario usuario,
            HttpServletRequest request
    ) {

        Usuario usuarioActual =
                validarAcceso(
                        request,
                        "USUARIOS_EDIT"
                );

        boolean nuevo =
                usuario.getIdUsuario() == null;

        /*
         * La identidad del actor se establece en backend.
         * No se confía en valores de auditoría enviados
         * desde el frontend.
         */

        Usuario guardado =
                usuarioService.guardar(
                        usuario
                );

        String accion =
                nuevo
                        ? "CREAR_USUARIO"
                        : "ACTUALIZAR_USUARIO";

        String descripcion =
                nuevo
                        ? "Se creó el usuario "
                        + guardado.getUsername()
                        : "Se actualizó el usuario "
                        + guardado.getUsername();

        /*
         * El idUsuario del log corresponde al actor
         * que realizó la operación.
         */
        logEventoService.registrarEvento(
                usuarioActual.getIdUsuario(),
                "SEGURIDAD",
                accion,
                descripcion,
                request
        );

        return ResponseEntity.ok(
                usuarioService.buscarDtoPorId(
                        guardado.getIdUsuario()
                )
        );
    }

    /**
     * Obtiene las agencias autorizadas para un usuario.
     */
    @GetMapping("/{id}/agencias")
    public ResponseEntity<List<Map<String, Object>>> listarAgencias(
            @PathVariable Integer id,
            HttpServletRequest request
    ) {

        validarAcceso(
                request,
                "USUARIOS_VIEW"
        );

        return ResponseEntity.ok(
                usuarioAgenciaService
                        .listarAgenciasDelUsuario(id)
        );
    }

    /**
     * Reemplaza las agencias autorizadas para un usuario.
     *
     * Una lista vacía elimina todas las asignaciones
     * de agencias del usuario.
     */
    @PostMapping("/{id}/agencias")
    public ResponseEntity<Void> asignarAgencias(
            @PathVariable Integer id,
            @RequestBody List<Integer> agencias,
            HttpServletRequest request
    ) {

        Usuario usuarioActual =
                validarAcceso(
                        request,
                        "USUARIOS_EDIT"
                );

        usuarioAgenciaService.asignarAgencias(
                id,
                agencias,
                usuarioActual.getIdUsuario()
        );

        logEventoService.registrarEvento(
                usuarioActual.getIdUsuario(),
                "SEGURIDAD",
                "ASIGNAR_AGENCIAS",
                "Se actualizaron las agencias autorizadas del usuario "
                        + id
                        + ": "
                        + agencias,
                request
        );

        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene los roles actualmente vigentes de un usuario.
     */
    @GetMapping("/{id}/roles")
    public ResponseEntity<List<UsuarioListDTO.RolDTO>> listarRoles(
            @PathVariable Integer id,
            HttpServletRequest request
    ) {

        validarAcceso(
                request,
                "USUARIOS_VIEW"
        );

        List<UsuarioListDTO.RolDTO> roles =
                usuarioRolService
                        .listarVigentes(id)
                        .stream()
                        .map(usuarioRol ->
                                UsuarioListDTO.RolDTO
                                        .builder()
                                        .idRol(
                                                usuarioRol
                                                        .getRol()
                                                        .getIdRol()
                                        )
                                        .nombreRol(
                                                usuarioRol
                                                        .getRol()
                                                        .getNombreRol()
                                        )
                                        .build()
                        )
                        .toList();

        return ResponseEntity.ok(
                roles
        );
    }

    /**
     * Asigna un rol al usuario.
     */
    @PostMapping("/{idUsuario}/roles/{idRol}")
    public ResponseEntity<UsuarioListDTO> asignarRol(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idRol,
            HttpServletRequest request
    ) {

        Usuario usuarioActual =
                validarAcceso(
                        request,
                        "USUARIOS_EDIT"
                );

        usuarioRolService.asignar(
                idUsuario,
                idRol,
                usuarioActual.getIdUsuario()
        );

        logEventoService.registrarEvento(
                usuarioActual.getIdUsuario(),
                "SEGURIDAD",
                "ASIGNAR_ROL",
                "Se asignó el rol "
                        + idRol
                        + " al usuario "
                        + idUsuario,
                request
        );

        return ResponseEntity.ok(
                usuarioService.buscarDtoPorId(
                        idUsuario
                )
        );
    }

    /**
     * Retira un rol activo del usuario.
     *
     * La asignación no se elimina físicamente.
     * Se conserva su histórico en usuarios_roles.
     */
    @DeleteMapping("/{idUsuario}/roles/{idRol}")
    public ResponseEntity<UsuarioListDTO> retirarRol(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idRol,
            HttpServletRequest request
    ) {

        Usuario usuarioActual =
                validarAcceso(
                        request,
                        "USUARIOS_EDIT"
                );

        usuarioRolService.retirar(
                idUsuario,
                idRol,
                usuarioActual.getIdUsuario()
        );

        logEventoService.registrarEvento(
                usuarioActual.getIdUsuario(),
                "SEGURIDAD",
                "RETIRAR_ROL",
                "Se retiró el rol "
                        + idRol
                        + " al usuario "
                        + idUsuario,
                request
        );

        return ResponseEntity.ok(
                usuarioService.buscarDtoPorId(
                        idUsuario
                )
        );
    }

    /**
     * Obtiene el usuario autenticado y valida
     * el permiso funcional requerido.
     */
    private Usuario validarAcceso(
            HttpServletRequest request,
            String permiso
    ) {

        Integer idUsuario = usuarioSesionService.idUsuario();

        Usuario usuarioActual = usuarioService.buscarPorId(idUsuario)
                .orElseThrow(() -> new SecurityException(
                        "El usuario autenticado no existe."
                ));

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );

        return usuarioActual;
    }
}
