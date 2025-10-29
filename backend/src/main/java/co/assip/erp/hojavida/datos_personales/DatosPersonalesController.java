package co.assip.erp.hojavida.datos_personales;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para la gestión de Datos Personales.
 *
 * Endpoint base: /api/v1/hoja-vida/datos-personales
 * (El prefijo /api/v1 es agregado automáticamente por la configuración global del proyecto).
 *
 * Funcionalidades:
 *  - GET    /hoja-vida/datos-personales         → Listar todos
 *  - GET    /hoja-vida/datos-personales/{id}    → Buscar por ID
 *  - POST   /hoja-vida/datos-personales         → Crear nuevo registro
 *  - PUT    /hoja-vida/datos-personales/{id}    → Actualizar existente
 *  - DELETE /hoja-vida/datos-personales/{id}    → Eliminar registro
 */
@RestController
@RequestMapping("/hoja-vida/datos-personales")
public class DatosPersonalesController {

    private final DatosPersonalesService service;

    public DatosPersonalesController(DatosPersonalesService service) {
        this.service = service;
    }

    /**
     * Lista todos los registros de datos personales.
     */
    @GetMapping
    public List<DatosPersonales> listar() {
        return service.listar();
    }

    /**
     * Busca un registro de datos personales por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DatosPersonales> buscarPorId(@PathVariable Integer id) {
        Optional<DatosPersonales> dato = service.buscarPorId(id);
        return dato.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo registro de datos personales.
     */
    @PostMapping
    public ResponseEntity<DatosPersonales> crear(@RequestBody DatosPersonales nuevo) {
        DatosPersonales guardado = service.crear(nuevo);
        return ResponseEntity.ok(guardado);
    }

    /**
     * Actualiza un registro existente por su ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DatosPersonales> actualizar(@PathVariable Integer id, @RequestBody DatosPersonales actualizado) {
        return service.actualizar(id, actualizado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Elimina un registro por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (service.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
