package co.assip.erp.seguridad.rolespermisos;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.rolespermisos.dto.RolPermisoDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST para administrar la asignación de permisos a roles.
 *
 * Este proceso modifica exclusivamente las relaciones almacenadas
 * en seguridad.rol_permisos y nunca elimina permisos del catálogo.
 */
@RestController
@RequestMapping("/seguridad/rol-permisos")
@RequiredArgsConstructor
public class RolPermisoController {

    private final RolPermisoService rolPermisoService;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    /**
     * Consulta los permisos asignados a un rol.
     *
     * Requiere el permiso ROLES_VIEW.
     */
    @GetMapping("/{idRol}")
    public ResponseEntity<List<RolPermisoDTO>> listar(
            @PathVariable Integer idRol,
            HttpServletRequest request
    ) {

        validarAcceso(request, "ROLES_VIEW");

        return ResponseEntity.ok(
                rolPermisoService.listarPermisos(idRol)
        );
    }

    /**
     * Reemplaza la configuración completa de permisos de un rol.
     *
     * El cuerpo contiene los identificadores de los permisos
     * que deben quedar asignados al rol.
     *
     * Requiere el permiso ROLES_EDIT.
     */
    @PutMapping("/{idRol}")
    public ResponseEntity<List<RolPermisoDTO>> asignar(
            @PathVariable Integer idRol,
            @RequestBody List<Integer> idPermisos,
            HttpServletRequest request
    ) {

        validarAcceso(request, "ROLES_EDIT");

        return ResponseEntity.ok(
                rolPermisoService.asignarPermisos(
                        idRol,
                        idPermisos
                )
        );
    }

    /**
     * Obtiene el usuario autenticado y valida el permiso requerido
     * para ejecutar la operación.
     */
    private void validarAcceso(
            HttpServletRequest request,
            String permiso
    ) {

        Integer idUsuario = usuarioSesionService.idUsuario();

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(idUsuario);

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}