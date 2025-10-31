package co.assip.erp.hojavida.permisosespeciales;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 🌐 Controlador REST — Permisos Especiales
 * Esquema: hoja_vida
 *
 * Rutas:
 *  - GET    /api/v1/hoja-vida/permisos-especiales
 *  - GET    /api/v1/hoja-vida/permisos-especiales/{id}
 *  - GET    /api/v1/hoja-vida/permisos-especiales/persona/{idDatosPersonal}
 *  - POST   /api/v1/hoja-vida/permisos-especiales
 *  - PUT    /api/v1/hoja-vida/permisos-especiales/{id}
 *  - DELETE /api/v1/hoja-vida/permisos-especiales/{id}
 *
 * Cada permiso (llamadas, sms, emails, cartas, redes) tiene
 * una fecha asociada de otorgamiento o cambio.
 */
@RestController
@RequestMapping("/hoja-vida/permisos-especiales")
public class PermisosEspecialesController {

    private final PermisosEspecialesRepository repository;

    public PermisosEspecialesController(PermisosEspecialesRepository repository) {
        this.repository = repository;
    }

    // ==========================================================
    // 🔹 LISTAR TODOS
    // ==========================================================
    @GetMapping
    public ResponseEntity<List<PermisosEspeciales>> listar() {
        return ResponseEntity.ok(repository.findAll());
    }

    // ==========================================================
    // 🔹 OBTENER POR ID
    // ==========================================================
    @GetMapping("/{id}")
    public ResponseEntity<PermisosEspeciales> obtener(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // 🔹 OBTENER POR ID DE PERSONA
    // ==========================================================
    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<PermisosEspeciales> obtenerPorPersona(@PathVariable Integer idDatosPersonal) {
        return repository.findByIdDatosPersonal(idDatosPersonal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // 🔹 CREAR NUEVO REGISTRO
    // ==========================================================
    @PostMapping
    public ResponseEntity<PermisosEspeciales> crear(@RequestBody PermisosEspeciales permiso) {
        // Verificar duplicado (1:1 con persona)
        if (repository.findByIdDatosPersonal(permiso.getIdDatosPersonal()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        LocalDate hoy = LocalDate.now();

        if (Boolean.TRUE.equals(permiso.getRecibeLlamadas())) permiso.setFechaLlamadas(hoy);
        if (Boolean.TRUE.equals(permiso.getRecibeMsm())) permiso.setFechaSms(hoy);
        if (Boolean.TRUE.equals(permiso.getRecibeEmails())) permiso.setFechaEmails(hoy);
        if (Boolean.TRUE.equals(permiso.getRecibeCartas())) permiso.setFechaCartas(hoy);
        if (Boolean.TRUE.equals(permiso.getRecibeRedesSociales())) permiso.setFechaRedes(hoy);

        PermisosEspeciales nuevo = repository.save(permiso);
        return ResponseEntity.ok(nuevo);
    }

    // ==========================================================
    // 🔹 ACTUALIZAR REGISTRO
    // ==========================================================
    @PutMapping("/{id}")
    public ResponseEntity<PermisosEspeciales> actualizar(@PathVariable Integer id,
                                                         @RequestBody PermisosEspeciales permiso) {
        return repository.findById(id)
                .map(existente -> {
                    LocalDate hoy = LocalDate.now();

                    // 🔸 Actualizar fechas solo si el valor del permiso cambió
                    if (!permiso.getRecibeLlamadas().equals(existente.getRecibeLlamadas())) {
                        permiso.setFechaLlamadas(hoy);
                    } else {
                        permiso.setFechaLlamadas(existente.getFechaLlamadas());
                    }

                    if (!permiso.getRecibeMsm().equals(existente.getRecibeMsm())) {
                        permiso.setFechaSms(hoy);
                    } else {
                        permiso.setFechaSms(existente.getFechaSms());
                    }

                    if (!permiso.getRecibeEmails().equals(existente.getRecibeEmails())) {
                        permiso.setFechaEmails(hoy);
                    } else {
                        permiso.setFechaEmails(existente.getFechaEmails());
                    }

                    if (!permiso.getRecibeCartas().equals(existente.getRecibeCartas())) {
                        permiso.setFechaCartas(hoy);
                    } else {
                        permiso.setFechaCartas(existente.getFechaCartas());
                    }

                    if (!permiso.getRecibeRedesSociales().equals(existente.getRecibeRedesSociales())) {
                        permiso.setFechaRedes(hoy);
                    } else {
                        permiso.setFechaRedes(existente.getFechaRedes());
                    }

                    // Mantener datos de auditoría
                    permiso.setIdPermisoEspecial(id);
                    permiso.setFechaCreacion(existente.getFechaCreacion());

                    return ResponseEntity.ok(repository.save(permiso));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // 🔹 ELIMINAR REGISTRO
    // ==========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
