package co.assip.erp.seguridad.roles;

import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.usuarios.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST para la administración de roles de seguridad.
 *
 * Un rol representa una responsabilidad funcional dentro del ERP.
 * La asignación de permisos y la asignación de usuarios se administran
 * mediante sus procesos correspondientes.
 */
@RestController
@RequestMapping("/seguridad/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;
    private final UsuarioService usuarioService;
    private final AccessValidator accessValidator;

    /**
     * Consulta todos los roles registrados.
     *
     * Requiere el permiso ROLES_VIEW.
     */
    @GetMapping
    public ResponseEntity<List<Rol>> listar(HttpServletRequest request) {

        validarAcceso(request, "ROLES_VIEW");

        return ResponseEntity.ok(
                rolService.listar()
        );
    }

    /**
     * Consulta un rol por su identificador.
     *
     * Requiere el permiso ROLES_VIEW.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Rol> buscar(
            @PathVariable Integer id,
            HttpServletRequest request
    ) {

        validarAcceso(request, "ROLES_VIEW");

        return ResponseEntity.ok(
                rolService.buscarPorId(id)
        );
    }

    /**
     * Crea un nuevo rol.
     *
     * Requiere el permiso ROLES_EDIT.
     */
    @PostMapping
    public ResponseEntity<Rol> crear(
            @RequestBody Rol rol,
            HttpServletRequest request
    ) {

        validarAcceso(request, "ROLES_EDIT");

        return ResponseEntity.ok(
                rolService.crear(rol)
        );
    }

    /**
     * Actualiza los datos de un rol existente.
     *
     * No modifica sus permisos ni los usuarios asociados.
     * Requiere el permiso ROLES_EDIT.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Rol> actualizar(
            @PathVariable Integer id,
            @RequestBody Rol rol,
            HttpServletRequest request
    ) {

        validarAcceso(request, "ROLES_EDIT");

        return ResponseEntity.ok(
                rolService.actualizar(id, rol)
        );
    }

    /**
     * Activa o desactiva un rol sin eliminarlo físicamente.
     *
     * Requiere el permiso ROLES_EDIT.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Rol> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam boolean activo,
            HttpServletRequest request
    ) {

        validarAcceso(request, "ROLES_EDIT");

        return ResponseEntity.ok(
                rolService.cambiarEstado(id, activo)
        );
    }

    /**
     * Obtiene el usuario autenticado y valida que posea el permiso
     * requerido para ejecutar la operación.
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