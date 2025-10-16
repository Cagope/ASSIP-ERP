package co.assip.erp.seguridad.service;

import co.assip.erp.seguridad.dto.UsuarioCreateRequest;
import co.assip.erp.seguridad.dto.UsuarioResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {

    Page<UsuarioResponse> listar(String q, Boolean activo, Boolean superuser, Pageable pageable,
                                 String actorUsername, String ip, String ua);

    UsuarioResponse detalle(Long id, String actorUsername, String ip, String ua);

    UsuarioResponse crear(UsuarioCreateRequest req, String actorUsername, String ip, String ua);

    // --- NUEVO: para cerrar el CRUD ---
    UsuarioResponse cambiarEstado(Long id, boolean activo, String actorUsername, String ip, String ua);

    void resetPassword(Long id, String nuevaPassword, String actorUsername, String ip, String ua);

    void forcePasswordChange(Long id, String actorUsername, String ip, String ua);
}
