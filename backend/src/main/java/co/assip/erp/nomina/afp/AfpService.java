package co.assip.erp.nomina.afp;

import co.assip.erp.nomina.afp.dto.AfpDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AfpService {

    private final AfpRepository repository;

    public List<AfpDTO> listar() {
        return repository.listar();
    }

    public AfpDTO obtener(Integer id) {
        return repository.obtener(id)
                .orElseThrow(() -> new RuntimeException("AFP no encontrada"));
    }

    public Integer crear(AfpDTO dto, Integer idUsuario) {
        return repository.crear(dto, idUsuario);
    }

    public void actualizar(Integer id, AfpDTO dto, Integer idUsuario) {
        repository.actualizar(id, dto, idUsuario);
    }

    public void eliminar(Integer id) {
        repository.eliminar(id);
    }
}
