package co.assip.erp.hojavida.datosfamiliares;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;

/**
 * 🧭 Controlador REST — Gestión de Datos Familiares
 *
 * Endpoint base: /api/v1/hoja-vida/datos-familiares
 * (El prefijo /api/v1 lo agrega la configuración global).
 *
 * Funcionalidades:
 *  - GET    /hoja-vida/datos-familiares                → Listar todos
 *  - GET    /hoja-vida/datos-familiares/{id}           → Buscar por ID
 *  - GET    /hoja-vida/datos-familiares/persona/{id}   → Listar por persona
 *  - GET    /hoja-vida/datos-familiares/parentesco/{c} → Listar por parentesco
 *  - GET    /hoja-vida/datos-familiares/referencias    → Listar referencias familiares
 *  - POST   /hoja-vida/datos-familiares                → Crear
 *  - PUT    /hoja-vida/datos-familiares/{id}           → Actualizar
 *  - DELETE /hoja-vida/datos-familiares/{id}           → Eliminar
 */
@RestController
@RequestMapping("/hoja-vida/datos-familiares")
public class DatosFamiliarController {

    private final DatosFamiliarService service;

    public DatosFamiliarController(DatosFamiliarService service) {
        this.service = service;
    }

    @GetMapping
    public List<DatosFamiliar> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatosFamiliar> buscarPorId(@PathVariable Integer id) {
        Optional<DatosFamiliar> item = service.buscarPorId(id);
        return item.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/persona/{id}")
    public ResponseEntity<List<DatosFamiliar>> listarPorPersona(@PathVariable Integer id) {
        List<DatosFamiliar> lista = service.listarPorPersona(id);
        if (lista.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/parentesco/{codigo}")
    public ResponseEntity<List<DatosFamiliar>> listarPorParentesco(@PathVariable String codigo) {
        List<DatosFamiliar> lista = service.listarPorParentesco(codigo);
        if (lista.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/referencias")
    public ResponseEntity<List<DatosFamiliar>> listarReferencias() {
        List<DatosFamiliar> lista = service.listarReferencias();
        if (lista.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<DatosFamiliar> crear(@RequestBody DatosFamiliar nuevo) {
        DatosFamiliar guardado = service.crear(nuevo);
        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DatosFamiliar> actualizar(@PathVariable Integer id, @RequestBody DatosFamiliar actualizado) {
        return service.actualizar(id, actualizado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (service.eliminar(id)) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }
}
