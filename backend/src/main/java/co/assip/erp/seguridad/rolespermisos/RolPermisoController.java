package co.assip.erp.seguridad.rolespermisos;

import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.permisos.Permiso;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.usuarios.UsuarioService;
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
    private final UsuarioService usuarioService;
    private final AccessValidator accessValidator;

    /**
     * Consulta los permisos asignados a un rol.
     *
     * Requiere el permiso ROLES_VIEW.
     */
    @GetMapping("/{idRol}")
    public ResponseEntity<List<Permiso>> listar(
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
    public ResponseEntity<List<Permiso>> asignar(
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

        Usuario usuarioActual =
                usuarioService.getUsuarioActual(request);

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}