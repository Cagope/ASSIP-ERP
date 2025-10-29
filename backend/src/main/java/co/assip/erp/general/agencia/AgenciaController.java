package co.assip.erp.general.agencia;

import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controlador REST para gestión de Agencias
 * Endpoint base: /api/v1/general/agencias
 */
@RestController
@RequestMapping("/general/agencias")
public class AgenciaController {

    private final AgenciaService service;

    public AgenciaController(AgenciaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Agencia> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Agencia obtenerPorId(@PathVariable Integer id) {
        return service.obtenerPorId(id);
    }

    @PostMapping
    public Agencia crear(@RequestBody Agencia agencia) {
        return service.guardar(agencia);
    }

    @PutMapping("/{id}")
    public Agencia actualizar(@PathVariable Integer id, @RequestBody Agencia agencia) {
        return service.actualizar(id, agencia);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}
