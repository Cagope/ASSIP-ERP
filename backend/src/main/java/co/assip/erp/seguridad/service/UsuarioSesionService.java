package co.assip.erp.seguridad.service;

import co.assip.erp.seguridad.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioSesionService {

    // ============================================================
    // ✅ Usuario autenticado
    // ============================================================
    public Integer idUsuario() {

        Integer idUsuario =
                SecurityUtils.getIdUsuario();

        if (idUsuario == null) {
            throw new RuntimeException(
                    "No se pudo identificar el usuario autenticado."
            );
        }

        return idUsuario;
    }

    // ============================================================
    // ✅ Rol autenticado
    // ============================================================
    public Integer idRol() {

        Integer idRol =
                SecurityUtils.getIdRol();

        return idRol != null ? idRol : 0;
    }

    // ============================================================
    // ✅ Agencias del usuario
    // ============================================================
    public List<Integer> agencias() {
        return SecurityUtils.getAgencias();
    }

    // ============================================================
    // ✅ Validar agencia
    // ============================================================
    public void validarAgencia(Integer idAgencia) {
        SecurityUtils.validarAgencia(idAgencia);
    }

    // ============================================================
    // ✅ Acceso total
    // ============================================================
    public boolean tieneAccesoTotal() {
        return SecurityUtils.tieneAccesoTotal();
    }

    // ============================================================
    // ✅ Pertenece a agencia
    // ============================================================
    public boolean perteneceA(Integer idAgencia) {
        return SecurityUtils.perteneceA(idAgencia);
    }

    public Integer agenciaPrincipal() {

        List<Integer> agencias = agencias();

        if (agencias == null || agencias.isEmpty()) {
            throw new RuntimeException(
                    "El usuario no tiene agencias asignadas."
            );
        }

        return agencias.get(0);
    }

}