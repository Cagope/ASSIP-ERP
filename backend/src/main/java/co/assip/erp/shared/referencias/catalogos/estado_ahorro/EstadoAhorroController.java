package co.assip.erp.shared.referencias.catalogos.estado_ahorro;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shared/estado-ahorro")
@RequiredArgsConstructor
public class EstadoAhorroController {

    private final EstadoAhorroService service;

    // ============================================================
    // 🔹 Listar todos
    // ============================================================
    @GetMapping
    public List<EstadoAhorro> listarTodos() {
        return service.listarTodos();
    }

    // ============================================================
    // 🔹 Solo operativos (para combos)
    // ============================================================
    @GetMapping("/operativos")
    public List<EstadoAhorro> listarOperativos() {
        return service.listarOperativos();
    }
}
