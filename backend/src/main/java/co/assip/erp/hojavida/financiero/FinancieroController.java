package co.assip.erp.hojavida.financieros;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 🌐 Controlador REST — Financieros
 * Esquema: hoja_vida
 *
 * Rutas:
 *  - GET    /api/v1/hoja-vida/financieros
 *  - GET    /api/v1/hoja-vida/financieros/{id}
 *  - GET    /api/v1/hoja-vida/financieros/persona/{idDatosPersonal}
 *  - POST   /api/v1/hoja-vida/financieros
 *  - PUT    /api/v1/hoja-vida/financieros/{id}
 *  - DELETE /api/v1/hoja-vida/financieros/{id}
 */
@RestController
@RequestMapping("/hoja-vida/financieros")
public class FinancieroController {

    private final FinancieroService service;

    public FinancieroController(FinancieroService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Financiero>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Financiero> obtener(@PathVariable Integer id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<Financiero> obtenerPorPersona(@PathVariable Integer idDatosPersonal) {
        return service.buscarPorPersona(idDatosPersonal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Financiero> crear(@RequestBody Financiero f) {
        try {
            return ResponseEntity.ok(service.crear(f));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Financiero> actualizar(@PathVariable Integer id, @RequestBody Financiero f) {
        return service.actualizar(id, f)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
