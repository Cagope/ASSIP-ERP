package co.assip.erp.seguridad.autorizacion;

import co.assip.erp.seguridad.rolespermisos.RolPermisoService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuariosroles.UsuarioRol;
import co.assip.erp.seguridad.usuariosroles.UsuarioRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validador central de autorización funcional del ERP.
 *
 * Los permisos efectivos de un usuario corresponden a la unión
 * de los permisos otorgados por todos sus roles vigentes.
 *
 * Los roles se obtienen desde seguridad.usuarios_roles y sus
 * permisos desde seguridad.rol_permisos.
 */
@Component
@RequiredArgsConstructor
public class AccessValidator {

    /**
     * Identificador actual del rol ADMIN.
     *
     * Se conserva temporalmente su acceso total mientras
     * se completa la matriz definitiva de permisos del ERP.
     */
    private static final Integer ID_ROL_ADMIN = 1;

    private final UsuarioRolService usuarioRolService;
    private final RolPermisoService rolPermisoService;

    /**
     * Valida que el usuario autenticado tenga autorización
     * para ejecutar una operación protegida.
     *
     * Basta con que uno de sus roles vigentes posea
     * el permiso solicitado.
     *
     * @param usuario       usuario autenticado
     * @param codigoPermiso código funcional requerido
     * @throws IllegalArgumentException si los datos recibidos
     *                                  no son válidos
     * @throws SecurityException        si el usuario no posee
     *                                  el permiso requerido
     */
    public void validarAcceso(
            Usuario usuario,
            String codigoPermiso
    ) {

        if (usuario == null
                || usuario.getIdUsuario() == null) {

            throw new IllegalArgumentException(
                    "No se encontró un usuario autenticado."
            );
        }

        if (codigoPermiso == null
                || codigoPermiso.isBlank()) {

            throw new IllegalArgumentException(
                    "El código del permiso es obligatorio."
            );
        }

        String permisoNormalizado =
                codigoPermiso.trim().toUpperCase();

        List<UsuarioRol> rolesVigentes =
                usuarioRolService.listarVigentes(
                        usuario.getIdUsuario()
                );

        if (rolesVigentes.isEmpty()) {
            throw new SecurityException(
                    "Acceso denegado. El usuario no tiene roles vigentes."
            );
        }

        /*
         * ADMIN conserva acceso administrativo total.
         *
         * La condición se determina por una asignación vigente
         * en usuarios_roles y no por usuarios.id_rol.
         */
        boolean esAdmin =
                rolesVigentes.stream()
                        .anyMatch(usuarioRol ->
                                ID_ROL_ADMIN.equals(
                                        usuarioRol
                                                .getRol()
                                                .getIdRol()
                                )
                        );

        if (esAdmin) {
            return;
        }

        /*
         * Los permisos efectivos son acumulativos.
         *
         * Si cualquiera de los roles vigentes contiene
         * el permiso solicitado, el usuario queda autorizado.
         */
        boolean autorizado =
                rolesVigentes.stream()
                        .anyMatch(usuarioRol ->
                                rolPermisoService
                                        .rolTienePermiso(
                                                usuarioRol
                                                        .getRol()
                                                        .getIdRol(),
                                                permisoNormalizado
                                        )
                        );

        if (!autorizado) {
            throw new SecurityException(
                    "Acceso denegado. El usuario no tiene el permiso "
                            + permisoNormalizado + "."
            );
        }
    }
}