package co.assip.erp.nomina.eps;

import co.assip.erp.nomina.eps.dto.EpsDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EpsService {

    private final EpsRepository repo;
    private final UsuarioSesionService usuarioSesionService;

    public List<EpsDTO> listar() {
        return repo.listar();
    }

    public EpsDTO obtener(Integer id) {
        return repo.obtener(id)
                .orElseThrow(() -> new RuntimeException("EPS no encontrada: " + id));
    }

    public Integer crear(EpsDTO dto) {
        Integer idUsuario =
                usuarioSesionService.idUsuario();
        return repo.crear(dto, idUsuario);
    }

    public void actualizar(Integer id, EpsDTO dto) {
        Integer idUsuario =
                usuarioSesionService.idUsuario();
        repo.actualizar(id, dto, idUsuario);
    }

    public void eliminar(Integer id) {
        repo.eliminar(id);
    }
}
