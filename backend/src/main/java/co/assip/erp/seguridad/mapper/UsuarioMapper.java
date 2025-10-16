package co.assip.erp.seguridad.mapper;

import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.dto.UsuarioResponse;

public final class UsuarioMapper {
    private UsuarioMapper() { }
    public static UsuarioResponse toResponse(Usuario u) {
        if (u == null) return null;
        return new UsuarioResponse(
                u.getId(),
                u.getUsername(),
                u.isSuperuserSeguridad(),
                u.isActivo(),
                u.getUltimoLoginEn()
        );
    }
}
