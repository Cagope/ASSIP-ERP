package co.assip.erp.seguridad.sesion;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

/**
 * Utilidades para consultar el contexto de seguridad
 * del usuario autenticado.
 *
 * La información se obtiene de los claims del JWT
 * almacenados por el filtro de autenticación en el
 * SecurityContext de Spring Security.
 */
public final class SecurityUtils {

    private static final Integer ID_ROL_ADMIN = 1;

    private SecurityUtils() {
    }

    /**
     * Obtiene los claims del JWT correspondiente
     * a la sesión autenticada.
     */
    private static Claims getClaims() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object credentials =
                authentication.getCredentials();

        if (credentials instanceof Claims claims) {
            return claims;
        }

        return null;
    }

    /**
     * Obtiene el identificador del usuario autenticado.
     *
     * @return id del usuario o null si no existe sesión válida
     */
    public static Integer getIdUsuario() {

        Claims claims = getClaims();

        if (claims == null) {
            return null;
        }

        return claims.get(
                "idUsuario",
                Integer.class
        );
    }

    /**
     * Obtiene las agencias incluidas en el contexto
     * de seguridad del usuario.
     */
    public static List<Integer> getAgencias() {

        Claims claims = getClaims();

        if (claims == null) {
            return Collections.emptyList();
        }

        Object valor = claims.get("agencias");

        if (!(valor instanceof List<?> lista)) {
            return Collections.emptyList();
        }

        try {
            return lista.stream()
                    .map(SecurityUtils::convertirAInteger)
                    .toList();

        } catch (RuntimeException ex) {
            throw new SecurityException(
                    "No fue posible obtener las agencias del usuario.",
                    ex
            );
        }
    }

    /**
     * Convierte un valor numérico proveniente del JWT
     * a Integer.
     */
    private static Integer convertirAInteger(
            Object valor
    ) {

        if (valor instanceof Integer integer) {
            return integer;
        }

        if (valor instanceof Number numero) {
            return numero.intValue();
        }

        throw new SecurityException(
                "El identificador de agencia del token no es válido."
        );
    }

    /**
     * Obtiene el identificador del rol autenticado.
     *
     * @return id del rol o null si no existe en el contexto
     */
    public static Integer getIdRol() {

        Claims claims = getClaims();

        if (claims == null) {
            return null;
        }

        Object valor = claims.get("rol");

        if (valor == null) {
            return null;
        }

        return convertirAInteger(valor);
    }

    /**
     * Valida que el usuario autenticado tenga acceso
     * a una agencia determinada.
     *
     * Si idAgencia es null no se realiza validación,
     * conservando el comportamiento actual del ERP.
     */
    public static void validarAgencia(
            Integer idAgencia
    ) {

        if (idAgencia == null) {
            return;
        }

        if (!perteneceA(idAgencia)) {
            throw new SecurityException(
                    "AGENCIA_NO_PERMITIDA"
            );
        }
    }

    /**
     * Indica si el usuario posee acceso administrativo total.
     *
     * Actualmente corresponde al rol ADMIN con id 1.
     */
    public static boolean tieneAccesoTotal() {

        Integer idRol = getIdRol();

        return ID_ROL_ADMIN.equals(idRol);
    }

    /**
     * Verifica si la agencia indicada pertenece al conjunto
     * de agencias autorizadas del usuario autenticado.
     */
    public static boolean perteneceA(
            Integer idAgencia
    ) {

        if (idAgencia == null) {
            return false;
        }

        return getAgencias().contains(idAgencia);
    }
}