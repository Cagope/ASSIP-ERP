    package co.assip.erp.hojavida.laboral;

    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import java.util.List;

    /**
     * 🌐 Controlador REST — Laboral
     * Esquema: hoja_vida
     *
     * Rutas:
     *  - GET    /api/v1/hoja-vida/laborales
     *  - GET    /api/v1/hoja-vida/laborales/{id}
     *  - GET    /api/v1/hoja-vida/laborales/persona/{idDatosPersonal}
     *  - POST   /api/v1/hoja-vida/laborales
     *  - PUT    /api/v1/hoja-vida/laborales/{id}
     *  - DELETE /api/v1/hoja-vida/laborales/{id}
     *
     * Descripción:
     * Maneja la información laboral asociada a cada persona.
     * Cada persona puede tener **solo un registro laboral** (relación 1:1).
     */
    @RestController
    @RequestMapping("/hoja-vida/laborales")
    public class LaboralController {

        private final LaboralService service;

        public LaboralController(LaboralService service) {
            this.service = service;
        }

        // ==========================================================
        // 🔹 LISTAR TODOS
        // ==========================================================
        @GetMapping
        public ResponseEntity<List<Laboral>> listar() {
            List<Laboral> lista = service.listar();
            return ResponseEntity.ok(lista);
        }

        // ==========================================================
        // 🔹 OBTENER POR ID
        // ==========================================================
        @GetMapping("/{id}")
        public ResponseEntity<Laboral> obtener(@PathVariable Integer id) {
            return service.buscarPorId(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        // ==========================================================
        // 🔹 OBTENER POR ID DE PERSONA
        // ==========================================================
        @GetMapping("/persona/{idDatosPersonal}")
        public ResponseEntity<Laboral> obtenerPorPersona(@PathVariable Integer idDatosPersonal) {
            return service.buscarPorPersona(idDatosPersonal)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        // ==========================================================
        // 🔹 CREAR NUEVO REGISTRO
        // ==========================================================
        @PostMapping
        public ResponseEntity<Laboral> crear(@RequestBody Laboral laboral) {
            try {
                Laboral creado = service.crear(laboral);
                return ResponseEntity.ok(creado);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        // ==========================================================
        // 🔹 ACTUALIZAR REGISTRO EXISTENTE
        // ==========================================================
        @PutMapping("/{id}")
        public ResponseEntity<Laboral> actualizar(@PathVariable Integer id, @RequestBody Laboral laboral) {
            return service.actualizar(id, laboral)
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
