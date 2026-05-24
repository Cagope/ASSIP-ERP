package co.assip.erp.nomina.secciones;

import co.assip.erp.nomina.secciones.dto.SeccionNominaDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeccionesService {

    private final SeccionesRepository repo;
    private final UsuarioSesionService usuarioSesionService;

    public List<SeccionNominaDTO> listar() {
        return repo.listar();
    }

    public SeccionNominaDTO obtener(Integer id) {
        return repo.obtener(id);
    }

    public void crear(SeccionNominaDTO dto) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        repo.crear(dto, idUsuario);
    }

    public void actualizar(Integer id, SeccionNominaDTO dto) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        repo.actualizar(id, dto, idUsuario);
    }

    public void eliminar(Integer id) {
        repo.eliminar(id);
    }
}
