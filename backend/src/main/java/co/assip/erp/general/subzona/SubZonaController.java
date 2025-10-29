package co.assip.erp.general.subzona;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/general/sub-zonas")
public class SubZonaController {

    private final SubZonaService service;

    public SubZonaController(SubZonaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SubZona>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubZona> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<SubZona> guardar(@RequestBody SubZona subZona) {
        return ResponseEntity.ok(service.guardar(subZona));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubZona> actualizar(@PathVariable Integer id, @RequestBody SubZona subZona) {
        return ResponseEntity.ok(service.actualizar(id, subZona));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
