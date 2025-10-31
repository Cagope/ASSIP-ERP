package co.assip.erp.hojavida.referenciaspersonales;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;

/**
 * 🧭 Controlador REST — Gestión de Referencias Personales
 *
 * Endpoint base: /api/v1/hoja-vida/referencias-personales
 * (El prefijo /api/v1 lo agrega automáticamente la configuración global del proyecto).
 *
 * Funcionalidades:
 *  - GET    /hoja-vida/referencias-personales                → Listar todos
 *  - GET    /hoja-vida/referencias-personales/{id}           → Buscar por ID
 *  - GET    /hoja-vida/referencias-personales/persona/{id}   → Listar por persona
 *  - POST   /hoja-vida/referencias-personales                → Crear nuevo registro
 *  - PUT    /hoja-vida/referencias-personales/{id}           → Actualizar existente
 *  - DELETE /hoja-vida/referencias-personales/{id}           → Eliminar registro
 */
@RestController
@RequestMapping("/hoja-vida/referencias-personales")
public class ReferenciaPersonalController {

    private final ReferenciaPersonalService service;

    public ReferenciaPersonalController(ReferenciaPersonalService service) {
        this.service = service;
    }

    /** 🔹 Listar todas las referencias personales */
    @GetMapping
    public List<ReferenciaPersonal> listar() {
        return service.listar();
    }

    /** 🔹 Buscar una referencia personal por su ID */
    @GetMapping("/{id}")
    public ResponseEntity<ReferenciaPersonal> buscarPorId(@PathVariable Integer id) {
        Optional<ReferenciaPersonal> ref = service.buscarPorId(id);
        return ref.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 🔹 Listar referencias personales por persona */
    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<List<ReferenciaPersonal>> listarPorPersona(@PathVariable Integer idDatosPersonal) {
        List<ReferenciaPersonal> lista = service.listarPorPersona(idDatosPersonal);
        if (lista.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(lista);
    }

    /** 🔹 Crear nueva referencia personal */
    @PostMapping
    public ResponseEntity<ReferenciaPersonal> crear(@RequestBody ReferenciaPersonal nueva) {
        ReferenciaPersonal guardada = service.crear(nueva);
        return ResponseEntity.ok(guardada);
    }

    /** 🔹 Actualizar referencia personal existente */
    @PutMapping("/{id}")
    public ResponseEntity<ReferenciaPersonal> actualizar(@PathVariable Integer id, @RequestBody ReferenciaPersonal actualizada) {
        return service.actualizar(id, actualizada)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 🔹 Eliminar referencia personal */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (service.eliminar(id)) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }
}
