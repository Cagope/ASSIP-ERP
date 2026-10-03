package co.assip.erp.seguridad.roles;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.roles.dto.RolDTO;
import co.assip.erp.seguridad.usuarios.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;

import java.util.List;

/**
 * API REST para la administración de roles de seguridad.
 *
 * Un rol representa una responsabilidad funcional dentro del ERP.
 *
 * La asignación de permisos y la asignación de usuarios se administran
 * mediante sus procesos correspondientes.
 *
 * Las respuestas REST utilizan DTO para evitar exponer directamente
 * entidades administradas por Hibernate.
 */
@RestController
@RequestMapping("/seguridad/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    /**
     * Consulta todos los roles registrados.
     *
     * Requiere el permiso ROLES_VIEW.
     */
    @GetMapping
    public ResponseEntity<List<RolDTO>> listar(
            HttpServletRequest request
    ) {

        validarAcceso(request, "ROLES_VIEW");

        return ResponseEntity.ok(
                rolService.listarDto()
        );
    }

    /**
     * Consulta un rol por su identificador.
     *
     * Requiere el permiso ROLES_VIEW.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RolDTO> buscar(
            @PathVariable Integer id,
            HttpServletRequest request
    ) {

        validarAcceso(request, "ROLES_VIEW");

        return ResponseEntity.ok(
                rolService.buscarDtoPorId(id)
        );
    }

    /**
     * Crea un nuevo rol.
     *
     * Requiere el permiso ROLES_EDIT.
     */
    @PostMapping
    public ResponseEntity<RolDTO> crear(
            @RequestBody Rol rol,
            HttpServletRequest request
    ) {

        validarAcceso(request, "ROLES_EDIT");

        Rol creado =
                rolService.crear(rol);

        return ResponseEntity.ok(
                rolService.convertirDto(creado)
        );
    }

    /**
     * Actualiza los datos de un rol existente.
     *
     * No modifica sus permisos ni los usuarios asociados.
     * Requiere el permiso ROLES_EDIT.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RolDTO> actualizar(
            @PathVariable Integer id,
            @RequestBody Rol rol,
            HttpServletRequest request
    ) {

        validarAcceso(request, "ROLES_EDIT");

        Rol actualizado =
                rolService.actualizar(
                        id,
                        rol
                );

        return ResponseEntity.ok(
                rolService.convertirDto(actualizado)
        );
    }

    /**
     * Activa o desactiva un rol sin eliminarlo físicamente.
     *
     * Requiere el permiso ROLES_EDIT.
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<RolDTO> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam boolean activo,
            HttpServletRequest request
    ) {

        validarAcceso(request, "ROLES_EDIT");

        Rol actualizado =
                rolService.cambiarEstado(
                        id,
                        activo
                );

        return ResponseEntity.ok(
                rolService.convertirDto(actualizado)
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

        Integer idUsuario = usuarioSesionService.idUsuario();

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(idUsuario);

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}