package co.assip.erp.hojavida.ubicaciones;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;

/**
 * 🧭 Controlador REST — Gestión de Ubicaciones
 *
 * Endpoint base: /api/v1/hoja-vida/ubicaciones
 * (El prefijo /api/v1 lo agrega automáticamente la configuración global del proyecto).
 *
 * Funcionalidades:
 *  - GET    /hoja-vida/ubicaciones              → Listar todos
 *  - GET    /hoja-vida/ubicaciones/{id}         → Buscar por ID
 *  - GET    /hoja-vida/ubicaciones/zona/{id}    → Listar por zona
 *  - GET    /hoja-vida/ubicaciones/subzona/{id} → Listar por subzona
 *  - POST   /hoja-vida/ubicaciones              → Crear nuevo registro
 *  - PUT    /hoja-vida/ubicaciones/{id}         → Actualizar existente
 *  - DELETE /hoja-vida/ubicaciones/{id}         → Eliminar registro
 */
@RestController
@RequestMapping("/hoja-vida/ubicaciones")
public class UbicacionController {

    private final UbicacionService service;

    public UbicacionController(UbicacionService service) {
        this.service = service;
    }

    /** 🔹 Listar todas las ubicaciones ordenadas por fecha de edición */
    @GetMapping
    public List<Ubicacion> listar() {
        return service.listar();
    }

    /** 🔹 Buscar una ubicación específica por su ID */
    @GetMapping("/{id}")
    public ResponseEntity<Ubicacion> buscarPorId(@PathVariable Integer id) {
        Optional<Ubicacion> ubicacion = service.buscarPorId(id);
        return ubicacion.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 🔹 Listar ubicaciones por zona */
    @GetMapping("/zona/{idZona}")
    public ResponseEntity<List<Ubicacion>> listarPorZona(@PathVariable Integer idZona) {
        List<Ubicacion> lista = service.listarPorZona(idZona);
        if (lista.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(lista);
    }

    /** 🔹 Listar ubicaciones por subzona */
    @GetMapping("/subzona/{idSubZona}")
    public ResponseEntity<List<Ubicacion>> listarPorSubZona(@PathVariable Integer idSubZona) {
        List<Ubicacion> lista = service.listarPorSubZona(idSubZona);
        if (lista.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(lista);
    }

    /** 🔹 Crear nueva ubicación */
    @PostMapping
    public ResponseEntity<Ubicacion> crear(@RequestBody Ubicacion nueva) {
        Ubicacion guardada = service.crear(nueva);
        return ResponseEntity.ok(guardada);
    }

    /** 🔹 Actualizar ubicación existente */
    @PutMapping("/{id}")
    public ResponseEntity<Ubicacion> actualizar(@PathVariable Integer id, @RequestBody Ubicacion actualizada) {
        return service.actualizar(id, actualizada)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 🔹 Eliminar ubicación */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (service.eliminar(id)) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }
}
