package co.assip.erp.nomina.arl;

import co.assip.erp.nomina.arl.dto.ArlDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArlService {

    private final ArlRepository repository;

    public List<ArlDTO> listar() {
        return repository.listar();
    }

    public ArlDTO obtener(Integer id) {
        return repository.obtener(id)
                .orElseThrow(() -> new RuntimeException("ARL no encontrada"));
    }

    public Integer crear(ArlDTO dto, Integer idUsuario) {
        return repository.crear(dto, idUsuario);
    }

    public void actualizar(Integer id, ArlDTO dto, Integer idUsuario) {
        repository.actualizar(id, dto, idUsuario);
    }

    public void eliminar(Integer id) {
        repository.eliminar(id);
    }
}
