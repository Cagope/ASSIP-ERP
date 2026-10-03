package co.assip.erp.seguridad.sesion;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

/**
 * Utilidades para consultar el contexto técnico
 * de la sesión autenticada.
 *
 * El JWT conserva únicamente información de identidad
 * y contexto de agencias.
 *
 * Los roles y permisos funcionales no se obtienen desde aquí.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Obtiene los claims asociados a la sesión autenticada.
     */
    private static Claims getClaims() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
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
     * @return id del usuario o null cuando no existe
     *         una sesión JWT válida
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
     * de la sesión autenticada.
     */
    public static List<Integer> getAgencias() {

        Claims claims = getClaims();

        if (claims == null) {
            return Collections.emptyList();
        }

        Object valor =
                claims.get("agencias");

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
     * Valida que la agencia indicada pertenezca
     * al contexto autorizado de la sesión.
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
     * Determina si una agencia pertenece al conjunto
     * de agencias incluido en la sesión.
     */
    public static boolean perteneceA(
            Integer idAgencia
    ) {

        if (idAgencia == null) {
            return false;
        }

        return getAgencias()
                .contains(idAgencia);
    }

    /**
     * Convierte un identificador numérico procedente
     * del JWT a Integer.
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
}