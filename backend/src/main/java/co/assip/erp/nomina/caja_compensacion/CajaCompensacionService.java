package co.assip.erp.nomina.caja_compensacion;

import co.assip.erp.nomina.caja_compensacion.dto.CajaCompensacionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CajaCompensacionService {

    private final CajaCompensacionRepository repository;

    public List<CajaCompensacionDTO> listar() {
        return repository.listar();
    }

    public CajaCompensacionDTO obtener(Integer id) {
        return repository.obtener(id)
                .orElseThrow(() -> new RuntimeException("Caja de Compensación no encontrada"));
    }

    public Integer crear(CajaCompensacionDTO dto, Integer idUsuario) {
        return repository.crear(dto, idUsuario);
    }

    public void actualizar(Integer id, CajaCompensacionDTO dto, Integer idUsuario) {
        repository.actualizar(id, dto, idUsuario);
    }

    public void eliminar(Integer id) {
        repository.eliminar(id);
    }
}
