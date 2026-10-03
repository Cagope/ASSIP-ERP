package co.assip.erp.hojavida.ubicaciones;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 🧭 Controlador REST — Gestión de Ubicaciones
 *
 * Endpoint base: /api/v1/hoja-vida/ubicaciones
 *
 * Seguridad:
 *  - GET                 → HOJAVIDA_PERSONA_VIEW
 *  - POST / PUT / DELETE → HOJAVIDA_PERSONA_EDIT
 */
@RestController
@RequestMapping("/hoja-vida/ubicaciones")
public class UbicacionController {

    private static final String PERMISO_VIEW =
            "HOJAVIDA_PERSONA_VIEW";

    private static final String PERMISO_EDIT =
            "HOJAVIDA_PERSONA_EDIT";

    private final UbicacionService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public UbicacionController(
            UbicacionService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    // =========================================================
    // CONSULTAS
    // =========================================================

    /**
     * 🔹 Listar todas las ubicaciones ordenadas por fecha de edición
     */
    @GetMapping
    public List<Ubicacion> listar() {

        validarAcceso(PERMISO_VIEW);

        return service.listar();
    }

    /**
     * 🔹 Buscar una ubicación específica por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ubicacion> buscarPorId(
            @PathVariable Integer id
    ) {

        validarAcceso(PERMISO_VIEW);

        Optional<Ubicacion> ubicacion =
                service.buscarPorId(id);

        return ubicacion
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 🔹 Listar ubicaciones por zona
     */
    @GetMapping("/zona/{idZona}")
    public ResponseEntity<List<Ubicacion>> listarPorZona(
            @PathVariable Integer idZona
    ) {

        validarAcceso(PERMISO_VIEW);

        List<Ubicacion> lista =
                service.listarPorZona(idZona);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    /**
     * 🔹 Listar ubicaciones por subzona
     */
    @GetMapping("/subzona/{idSubZona}")
    public ResponseEntity<List<Ubicacion>> listarPorSubZona(
            @PathVariable Integer idSubZona
    ) {

        validarAcceso(PERMISO_VIEW);

        List<Ubicacion> lista =
                service.listarPorSubZona(idSubZona);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    // =========================================================
    // MODIFICACIONES
    // =========================================================

    /**
     * 🔹 Crear nueva ubicación
     */
    @PostMapping
    public ResponseEntity<Ubicacion> crear(
            @RequestBody Ubicacion nueva
    ) {

        validarAcceso(PERMISO_EDIT);

        Ubicacion guardada =
                service.crear(nueva);

        return ResponseEntity.ok(guardada);
    }

    /**
     * 🔹 Actualizar ubicación existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<Ubicacion> actualizar(
            @PathVariable Integer id,
            @RequestBody Ubicacion actualizada
    ) {

        validarAcceso(PERMISO_EDIT);

        return service.actualizar(id, actualizada)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 🔹 Eliminar ubicación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer id
    ) {

        validarAcceso(PERMISO_EDIT);

        if (service.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarAcceso(String permiso) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(idUsuario);

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}