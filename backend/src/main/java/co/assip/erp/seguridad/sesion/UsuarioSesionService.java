package co.assip.erp.seguridad.sesion;

import co.assip.erp.seguridad.usuariosroles.UsuarioRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio central de acceso al contexto del usuario autenticado.
 *
 * Proporciona identidad, agencias y validaciones generales
 * utilizadas transversalmente por los módulos del ERP.
 *
 * Los roles no se obtienen del JWT. Las validaciones basadas
 * en roles consultan las asignaciones vigentes almacenadas
 * en seguridad.usuarios_roles.
 */
@Service
@RequiredArgsConstructor
public class UsuarioSesionService {

    /**
     * Identificador del rol ADMIN.
     *
     * Se conserva temporalmente mientras se completa
     * la matriz definitiva de permisos del ERP.
     */
    private static final Integer ID_ROL_ADMIN = 1;

    private final UsuarioRolService usuarioRolService;

    /**
     * Obtiene el identificador del usuario autenticado.
     *
     * @return identificador interno del usuario
     * @throws SecurityException cuando la sesión no permite
     *                           identificar al usuario
     */
    public Integer idUsuario() {

        Integer idUsuario =
                SecurityUtils.getIdUsuario();

        if (idUsuario == null) {
            throw new SecurityException(
                    "No se pudo identificar el usuario autenticado."
            );
        }

        return idUsuario;
    }

    /**
     * Obtiene las agencias incluidas en el contexto
     * de la sesión autenticada.
     */
    public List<Integer> agencias() {
        return SecurityUtils.getAgencias();
    }

    /**
     * Valida que el usuario autenticado tenga acceso
     * a una agencia determinada.
     */
    public void validarAgencia(
            Integer idAgencia
    ) {

        /*
         * ADMIN conserva acceso global aunque no sea necesario
         * incluir todas las agencias dentro del JWT.
         */
        if (tieneAccesoTotal()) {
            return;
        }

        SecurityUtils.validarAgencia(
                idAgencia
        );
    }

    /**
     * Determina si el usuario autenticado posee acceso
     * administrativo total.
     *
     * ADMIN se resuelve desde una asignación activa y vigente
     * en seguridad.usuarios_roles, no desde un claim del JWT.
     */
    public boolean tieneAccesoTotal() {

        return usuarioRolService.tieneRolVigente(
                idUsuario(),
                ID_ROL_ADMIN
        );
    }

    /**
     * Determina si el usuario tiene acceso a una agencia.
     *
     * ADMIN posee acceso global.
     */
    public boolean perteneceA(
            Integer idAgencia
    ) {

        if (idAgencia == null) {
            return false;
        }

        if (tieneAccesoTotal()) {
            return true;
        }

        return SecurityUtils.perteneceA(
                idAgencia
        );
    }

    /**
     * Obtiene la primera agencia disponible en el contexto
     * actual de sesión.
     *
     * Se conserva este comportamiento por compatibilidad con
     * los procesos existentes. Este valor no necesariamente
     * corresponde a seguridad.usuarios.id_agencia_principal.
     */
    public Integer agenciaPrincipal() {

        List<Integer> agencias =
                agencias();

        if (agencias.isEmpty()) {
            throw new SecurityException(
                    "El usuario no tiene agencias asignadas."
            );
        }

        return agencias.get(0);
    }
}