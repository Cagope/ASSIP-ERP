package co.assip.erp.nomina.variables_vigencia;

import co.assip.erp.nomina.variables_vigencia.dto.VariablesVigenciaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VariablesVigenciaService {

    private final VariablesVigenciaRepository repository;

    public List<VariablesVigenciaDTO> listar() {
        return repository.listar();
    }

    public VariablesVigenciaDTO obtener(Integer id) {
        return repository.obtener(id)
                .orElseThrow(() -> new RuntimeException("No existe Variables Vigencia con ID: " + id));
    }

    public Integer crear(VariablesVigenciaDTO dto, Integer idUsuario) {
        return repository.crear(dto, idUsuario);
    }

    public void actualizar(Integer id, VariablesVigenciaDTO dto, Integer idUsuario) {
        repository.actualizar(id, dto, idUsuario);
    }

    public void eliminar(Integer id) {
        repository.eliminar(id);
    }
}
