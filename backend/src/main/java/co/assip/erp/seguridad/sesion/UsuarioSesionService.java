package co.assip.erp.seguridad.sesion;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de acceso al contexto de seguridad
 * del usuario autenticado.
 *
 * Centraliza el acceso al identificador del usuario,
 * rol y agencias contenidos en la sesión JWT.
 */
@Service
public class UsuarioSesionService {

    /**
     * Obtiene el identificador del usuario autenticado.
     *
     * @return identificador del usuario
     * @throws SecurityException si no existe un usuario
     *                           identificado en la sesión
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
     * Obtiene el identificador del rol autenticado.
     *
     * Se conserva el valor 0 cuando la sesión no contiene
     * un rol para mantener el contrato actual del ERP.
     */
    public Integer idRol() {

        Integer idRol =
                SecurityUtils.getIdRol();

        return idRol != null
                ? idRol
                : 0;
    }

    /**
     * Obtiene las agencias incluidas en el contexto
     * de seguridad del usuario.
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

        SecurityUtils.validarAgencia(idAgencia);
    }

    /**
     * Indica si el usuario autenticado posee
     * acceso administrativo total.
     */
    public boolean tieneAccesoTotal() {
        return SecurityUtils.tieneAccesoTotal();
    }

    /**
     * Verifica si una agencia pertenece al conjunto
     * de agencias autorizadas del usuario.
     */
    public boolean perteneceA(
            Integer idAgencia
    ) {

        return SecurityUtils.perteneceA(idAgencia);
    }

    /**
     * Obtiene la primera agencia disponible en el
     * contexto de seguridad.
     *
     * Este método conserva el comportamiento existente.
     * No representa necesariamente id_agencia_principal
     * de seguridad.usuarios.
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