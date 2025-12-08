package co.assip.erp.shared.referencias.catalogos.estado_ahorro;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadoAhorroService {

    private final EstadoAhorroRepository repository;

    // ============================================================
    // 🔹 Listar todos los estados
    // ============================================================
    public List<EstadoAhorro> listarTodos() {
        return repository.listar();
    }

    // ============================================================
    // 🔹 Listar solo estados operativos (para combos)
    // ============================================================
    public List<EstadoAhorro> listarOperativos() {
        return repository.listarOperativos();
    }
}
