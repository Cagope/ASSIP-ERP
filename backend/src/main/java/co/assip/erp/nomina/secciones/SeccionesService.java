package co.assip.erp.nomina.secciones;

import co.assip.erp.nomina.secciones.dto.SeccionNominaDTO;
import co.assip.erp.seguridad.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeccionesService {

    private final SeccionesRepository repo;

    public List<SeccionNominaDTO> listar() {
        return repo.listar();
    }

    public SeccionNominaDTO obtener(Integer id) {
        return repo.obtener(id);
    }

    public void crear(SeccionNominaDTO dto) {
        Integer idUsuario = SecurityUtils.getIdUsuario();
        repo.crear(dto, idUsuario != null ? idUsuario : 1);
    }

    public void actualizar(Integer id, SeccionNominaDTO dto) {
        Integer idUsuario = SecurityUtils.getIdUsuario();
        repo.actualizar(id, dto, idUsuario != null ? idUsuario : 1);
    }

    public void eliminar(Integer id) {
        repo.eliminar(id);
    }
}
