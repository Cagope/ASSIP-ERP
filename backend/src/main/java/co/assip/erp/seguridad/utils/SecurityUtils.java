package co.assip.erp.seguridad.utils;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

public final class SecurityUtils {

    private SecurityUtils() {}

    // ============================================================
    // 🔐 Obtener Claims del JWT que el filtro guardó en SecurityContext
    // ============================================================
    private static Claims getClaims() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        Object credentials = auth.getCredentials();
        if (credentials instanceof Claims c) return c;

        return null;
    }

    // ============================================================
    // ✅ ID del usuario autenticado
    // ============================================================
    public static Integer getIdUsuario() {
        Claims c = getClaims();
        if (c == null) return null;
        return c.get("idUsuario", Integer.class);
    }

    // ============================================================
    // ✅ Lista de agencias permitidas en el token
    // ============================================================
    @SuppressWarnings("unchecked")
    public static List<Integer> getAgencias() {
        Claims c = getClaims();
        if (c == null) return Collections.emptyList();

        Object raw = c.get("agencias");
        if (raw instanceof List<?> list) {
            try {
                return (List<Integer>) list;
            } catch (Exception e) {
                throw new SecurityException(
                        "No fue posible obtener las agencias del usuario."
                );
            }
        }
        return Collections.emptyList();
    }

    // ============================================================
    // ✅ ID del rol autenticado
    // ============================================================
    public static Integer getIdRol() {
        Claims c = getClaims();
        if (c == null) return null;
        Integer rol = c.get("rol", Integer.class);
        return (rol != null) ? rol : 0;
    }

    // ============================================================
    // ✅ Validar agencia (si llega null, NO valida)
    // ============================================================
    public static void validarAgencia(Integer idAgencia) {
        if (idAgencia == null) return;

        List<Integer> agencias = getAgencias();
        if (agencias == null || agencias.isEmpty() || !agencias.contains(idAgencia)) {
            throw new RuntimeException("AGENCIA_NO_PERMITIDA");
        }
    }

    // ============================================================
    // ✅ Acceso total (por ahora: rol ADMIN = 1)
    // ============================================================
    public static boolean tieneAccesoTotal() {
        Integer rol = getIdRol();
        return rol != null && rol == 1; // ADMIN
    }

    // ============================================================
    // ✅ Verifica si el usuario pertenece a una agencia específica
    // ============================================================
    public static boolean perteneceA(Integer idAgencia) {
        if (idAgencia == null) return false;

        List<Integer> agencias = getAgencias();
        if (agencias == null || agencias.isEmpty()) return false;

        return agencias.contains(idAgencia);
    }


}
