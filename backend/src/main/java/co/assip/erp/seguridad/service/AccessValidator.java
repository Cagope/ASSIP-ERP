package co.assip.erp.seguridad.service;

import co.assip.erp.seguridad.domain.Usuario;
import org.springframework.stereotype.Component;

/**
 * Valida si un usuario tiene permiso para ejecutar una acción específica,
 * usando la tabla seguridad.rol_permisos.
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
     * Valida si el usuario tiene asignado un permiso por su código.
     * Lanza excepción si no está autorizado.
     */
    public void validarAcceso(Usuario usuario, String codigoPermiso) {

        // 🔥 Validación correcta con idRol
        if (usuario == null || usuario.getIdRol() == null) {
            throw new RuntimeException("Usuario sin rol válido");
        }

        // ⭐⭐⭐ BYPASS PARA ADMIN — SIN VALIDAR PERMISOS ⭐⭐⭐
        // Si id_rol = 1 (ADMIN), tiene acceso total
        if (usuario.getIdRol() == 1) {
            return;
        }

        // ⭐ Usuarios NO administradores → validar permisos
        boolean autorizado = rolPermisoService.rolTienePermiso(
                usuario.getIdRol(),
                codigoPermiso
        );

        if (!autorizado) {
            throw new RuntimeException(
                    "Acceso denegado: el rol " + usuario.getIdRol() +
                            " no tiene permiso " + codigoPermiso
            );
        }
    }
}
