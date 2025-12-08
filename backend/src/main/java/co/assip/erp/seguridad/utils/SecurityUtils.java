package co.assip.erp.seguridad.utils;

import io.jsonwebtoken.Claims;
import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

@UtilityClass
public class SecurityUtils {

    /**
     * Obtiene los Claims almacenados como Credentials
     * dentro del Authentication del usuario autenticado.
     *
     * Esto requiere que el filtro JWT del proyecto
     * coloque Claims como credentials del Authentication.
     */
    private Claims getClaims() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof UsernamePasswordAuthenticationToken token &&
                token.getCredentials() instanceof Claims claims) {
            return claims;
        }

        return null;  // No hay claims → sin datos
    }

    // ============================================================
    //    DATOS DEL TOKEN
    // ============================================================

    /** 🔹 ID del usuario autenticado (idUsuario en el JWT) */
    public Integer getIdUsuario() {
        Claims c = getClaims();
        return c != null ? c.get("idUsuario", Integer.class) : null;
    }

    /** 🔹 Username del token (sub) */
    public String getUsername() {
        Claims c = getClaims();
        return c != null ? c.getSubject() : null;
    }

    /** 🔹 Lista de agencias a las que pertenece el usuario */
    @SuppressWarnings("unchecked")
    public List<Integer> getAgencias() {
        Claims c = getClaims();
        if (c == null) return Collections.emptyList();

        Object valor = c.get("agencias");

        if (valor instanceof List<?> lista) {
            return (List<Integer>) lista;
        }

        return Collections.emptyList();
    }

    /** 🔹 ¿Acceso total? (si no hay agencias definidas) */
    public boolean tieneAccesoTotal() {
        List<Integer> ag = getAgencias();
        return ag == null || ag.isEmpty();
    }

    /** 🔹 ¿El usuario pertenece a una agencia dada? */
    public boolean perteneceA(Integer idAgencia) {
        if (tieneAccesoTotal()) return true;
        return getAgencias().contains(idAgencia);
    }
}
