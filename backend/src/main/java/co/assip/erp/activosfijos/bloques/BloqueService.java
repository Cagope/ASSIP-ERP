package co.assip.erp.activosfijos.bloques;

import co.assip.erp.activosfijos.bloques.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BloqueService {

    private final BloqueRepository repository;

    public List<BloqueListDTO> listar() {
        return repository.listar();
    }

    public BloqueFormDTO obtener(Integer id) {
        return repository.obtener(id);
    }

    public void crear(BloqueSaveDTO dto, Integer idUsuario) {
        repository.insertar(dto, idUsuario);
    }

    public void actualizar(Integer id, BloqueSaveDTO dto, Integer idUsuario) {
        repository.actualizar(id, dto, idUsuario);
    }

    public void eliminar(Integer id) {
        repository.eliminar(id);
    }
}
