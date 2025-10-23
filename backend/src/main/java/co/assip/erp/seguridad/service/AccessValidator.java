package co.assip.erp.seguridad.service;

import co.assip.erp.seguridad.domain.Usuario;
import org.springframework.stereotype.Component;

/**
 * Valida si un usuario tiene permiso para ejecutar una acción específica,
 * según los permisos asociados a su rol en la tabla seguridad.rol_permisos.
 *
 * Uso:
 *   accessValidator.validarAcceso(usuarioActual, "HOJAVIDA_EDIT");
 */
@Component
public class AccessValidator {

    private final RolPermisoService rolPermisoService;

    public AccessValidator(RolPermisoService rolPermisoService) {
        this.rolPermisoService = rolPermisoService;
    }

    /**
     * Valida si el rol del usuario tiene asignado un permiso por su código.
     * Lanza excepción si no está autorizado.
     */
    public void validarAcceso(Usuario usuario, String codigoPermiso) {
        if (usuario == null || usuario.getRol() == null) {
            throw new RuntimeException("Usuario o rol no válido");
        }

        boolean autorizado = rolPermisoService.rolTienePermiso(
                usuario.getRol().getIdRol(),  // ✅ corregido: idRol en lugar de id
                codigoPermiso
        );

        if (!autorizado) {
            throw new RuntimeException("Acceso denegado: el rol no tiene permiso " + codigoPermiso);
        }
    }
}
