package co.assip.erp.hojavida.sarlaft;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 🌐 Controlador REST — Sarlaft
 * Esquema: hoja_vida
 *
 * Rutas:
 *  - GET    /api/v1/hoja-vida/sarlaft
 *  - GET    /api/v1/hoja-vida/sarlaft/{id}
 *  - GET    /api/v1/hoja-vida/sarlaft/persona/{idDatosPersonal}
 *  - POST   /api/v1/hoja-vida/sarlaft
 *  - PUT    /api/v1/hoja-vida/sarlaft/{id}
 *  - DELETE /api/v1/hoja-vida/sarlaft/{id}
 *
 * Descripción:
 * Gestiona la información SARLAFT asociada a cada persona (relación 1:1).
 * Cada persona solo puede tener un registro SARLAFT activo.
 */
@RestController
@RequestMapping("/hoja-vida/sarlaft")
public class SarlaftController {

    private final SarlaftService service;

    public SarlaftController(SarlaftService service) {
        this.service = service;
    }

    // ==========================================================
    // 🔹 LISTAR TODOS
    // ==========================================================
    @GetMapping
    public ResponseEntity<List<Sarlaft>> listar() {
        List<Sarlaft> lista = service.listar();
        return ResponseEntity.ok(lista);
    }

    // ==========================================================
    // 🔹 OBTENER POR ID
    // ==========================================================
    @GetMapping("/{id}")
    public ResponseEntity<Sarlaft> obtener(@PathVariable Integer id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // 🔹 OBTENER POR ID DE PERSONA
    // ==========================================================
    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<Sarlaft> obtenerPorPersona(@PathVariable Integer idDatosPersonal) {
        return service.buscarPorPersona(idDatosPersonal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // 🔹 CREAR NUEVO REGISTRO
    // ==========================================================
    @PostMapping
    public ResponseEntity<Sarlaft> crear(@RequestBody Sarlaft sarlaft) {
        try {
            Sarlaft creado = service.crear(sarlaft);
            return ResponseEntity.ok(creado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ==========================================================
    // 🔹 ACTUALIZAR REGISTRO EXISTENTE
    // ==========================================================
    @PutMapping("/{id}")
    public ResponseEntity<Sarlaft> actualizar(@PathVariable Integer id, @RequestBody Sarlaft sarlaft) {
        return service.actualizar(id, sarlaft)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // 🔹 ELIMINAR REGISTRO
    // ==========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
