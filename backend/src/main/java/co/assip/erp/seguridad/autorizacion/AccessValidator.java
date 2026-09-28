package co.assip.erp.seguridad.autorizacion;

import co.assip.erp.seguridad.rolespermisos.RolPermisoService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validador central de autorización funcional del ERP.
 *
 * Determina si el rol del usuario posee el permiso requerido
 * para ejecutar una operación.
 *
 * Los permisos efectivos se obtienen de seguridad.rol_permisos.
 */
@Component
@RequiredArgsConstructor
public class AccessValidator {

    /**
     * Identificador actual del rol administrador.
     *
     * Se conserva temporalmente el acceso total del administrador
     * mientras se completa la normalización de permisos del ERP.
     */
    private static final Integer ID_ROL_ADMIN = 1;

    private final RolPermisoService rolPermisoService;

    /**
     * Valida que el usuario tenga autorización para ejecutar
     * una operación protegida por un permiso funcional.
     *
     * @param usuario       usuario autenticado
     * @param codigoPermiso código del permiso requerido
     * @throws IllegalArgumentException si el usuario, rol o permiso
     *                                  recibido no es válido
     * @throws SecurityException        si el rol no posee el permiso
     */
    public void validarAcceso(
            Usuario usuario,
            String codigoPermiso
    ) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "No se encontró un usuario autenticado."
            );
        }

        if (usuario.getIdRol() == null) {
            throw new IllegalArgumentException(
                    "El usuario no tiene un rol asignado."
            );
        }

        if (codigoPermiso == null
                || codigoPermiso.isBlank()) {

            throw new IllegalArgumentException(
                    "El código del permiso es obligatorio."
            );
        }

        /*
         * Acceso administrativo total.
         *
         * Este bypass se conserva temporalmente mientras
         * se completa la matriz definitiva de permisos.
         */
        if (ID_ROL_ADMIN.equals(usuario.getIdRol())) {
            return;
        }

        String permisoNormalizado =
                codigoPermiso.trim().toUpperCase();

        boolean autorizado =
                rolPermisoService.rolTienePermiso(
                        usuario.getIdRol(),
                        permisoNormalizado
                );

        if (!autorizado) {
            throw new SecurityException(
                    "Acceso denegado. El usuario no tiene el permiso "
                            + permisoNormalizado + "."
            );
        }
    }
}