package co.assip.erp.depositos.formasahorro;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/depositos/formas-ahorro")
public class FormaAhorroController {

    private final FormaAhorroService service;

    public FormaAhorroController(FormaAhorroService service) {
        this.service = service;
    }

    // ==========================================================
    // LISTAR TODAS
    // ==========================================================
    @GetMapping
    public List<FormaAhorro> listar() {
        return service.listar();
    }

    // ==========================================================
    // OBTENER UNA
    // ==========================================================
    @GetMapping("/{id}")
    public FormaAhorro obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    // ==========================================================
    // CREAR
    // ==========================================================
    @PostMapping
    public FormaAhorro crear(@RequestBody FormaAhorro f) {
        return service.crear(f);
    }

    // ==========================================================
    // EDITAR
    // ==========================================================
    @PutMapping("/{id}")
    public FormaAhorro editar(@PathVariable Integer id,
                              @RequestBody FormaAhorro f) {
        return service.editar(id, f);
    }

    // ==========================================================
    // ELIMINAR
    // ==========================================================
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}
