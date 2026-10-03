package co.assip.erp.seguridad.permisos;

import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;

import java.util.List;

/**
 * API REST para la administración del catálogo de permisos del ERP.
 *
 * Un permiso representa una capacidad funcional del sistema.
 * La asignación de permisos a roles se administra mediante
 * el proceso Roles-Permisos.
 */
@RestController
@RequestMapping("/seguridad/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final PermisoService permisoService;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    /**
     * Consulta todos los permisos registrados.
     *
     * Requiere el permiso PERMISOS_VIEW.
     */
    @GetMapping
    public ResponseEntity<List<Permiso>> listar(
            HttpServletRequest request
    ) {

        validarAcceso(request, "PERMISOS_VIEW");

        return ResponseEntity.ok(
                permisoService.listar()
        );
    }

    /**
     * Consulta un permiso por su identificador.
     *
     * Requiere el permiso PERMISOS_VIEW.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Permiso> buscar(
            @PathVariable Integer id,
            HttpServletRequest request
    ) {

        validarAcceso(request, "PERMISOS_VIEW");

        return ResponseEntity.ok(
                permisoService.buscarPorId(id)
        );
    }

    /**
     * Crea un nuevo permiso en el catálogo.
     *
     * La creación del permiso no lo asigna automáticamente
     * a ningún rol.
     *
     * Requiere el permiso PERMISOS_EDIT.
     */
    @PostMapping
    public ResponseEntity<Permiso> crear(
            @RequestBody Permiso permiso,
            HttpServletRequest request
    ) {

        validarAcceso(request, "PERMISOS_EDIT");

        return ResponseEntity.ok(
                permisoService.crear(permiso)
        );
    }

    /**
     * Actualiza los datos de un permiso existente.
     *
     * No modifica los roles que tienen asignado el permiso.
     *
     * Requiere el permiso PERMISOS_EDIT.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Permiso> actualizar(
            @PathVariable Integer id,
            @RequestBody Permiso permiso,
            HttpServletRequest request
    ) {

        validarAcceso(request, "PERMISOS_EDIT");

        return ResponseEntity.ok(
                permisoService.actualizar(id, permiso)
        );
    }

    /**
     * Activa o desactiva un permiso sin eliminarlo físicamente.
     *
     * Las relaciones existentes con roles se conservan.
     *
     * Requiere el permiso PERMISOS_EDIT.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Permiso> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam boolean activo,
            HttpServletRequest request
    ) {

        validarAcceso(request, "PERMISOS_EDIT");

        return ResponseEntity.ok(
                permisoService.cambiarEstado(id, activo)
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