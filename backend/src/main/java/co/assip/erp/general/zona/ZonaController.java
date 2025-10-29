package co.assip.erp.general.zona;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/general/zonas")
public class ZonaController {

    private final ZonaService service;

    public ZonaController(ZonaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Zona>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Zona> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<Zona> guardar(@RequestBody Zona zona) {
        return ResponseEntity.ok(service.guardar(zona));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Zona> actualizar(@PathVariable Integer id, @RequestBody Zona zona) {
        return ResponseEntity.ok(service.actualizar(id, zona));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
