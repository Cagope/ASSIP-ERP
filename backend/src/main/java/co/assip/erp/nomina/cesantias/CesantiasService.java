package co.assip.erp.nomina.cesantias;

import co.assip.erp.nomina.cesantias.dto.CesantiasDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CesantiasService {

    private final CesantiasRepository repository;

    // ============================================================
    // ✅ LISTAR
    // ============================================================
    public List<CesantiasDTO> listar() {
        return repository.listar();
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    public CesantiasDTO obtener(Integer id) {
        return repository.obtener(id)
                .orElseThrow(() ->
                        new RuntimeException("Fondo de cesantías no encontrado"));
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    public void crear(CesantiasDTO dto, Integer idUsuario) {
        repository.crear(dto, idUsuario);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    public void actualizar(Integer id, CesantiasDTO dto, Integer idUsuario) {
        repository.actualizar(id, dto, idUsuario);
    }

    // ============================================================
    // ✅ ELIMINAR
    // ============================================================
    public void eliminar(Integer id) {
        repository.eliminar(id);
    }
}
