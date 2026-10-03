package co.assip.erp.hojavida.referenciaspersonales;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 🧭 Controlador REST — Gestión de Referencias Personales
 *
 * Endpoint base:
 * /api/v1/hoja-vida/referencias-personales
 *
 * Seguridad:
 * - Consulta: HOJAVIDA_PERSONA_VIEW
 * - Creación, actualización y eliminación:
 *   HOJAVIDA_PERSONA_EDIT
 */
@RestController
@RequestMapping("/hoja-vida/referencias-personales")
public class ReferenciaPersonalController {

    private static final String PERMISO_VIEW =
            "HOJAVIDA_PERSONA_VIEW";

    private static final String PERMISO_EDIT =
            "HOJAVIDA_PERSONA_EDIT";

    private final ReferenciaPersonalService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public ReferenciaPersonalController(
            ReferenciaPersonalService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    /**
     * Listar todas las referencias personales.
     */
    @GetMapping
    public List<ReferenciaPersonal> listar() {

        validarAcceso(PERMISO_VIEW);

        return service.listar();
    }

    /**
     * Buscar una referencia personal por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReferenciaPersonal> buscarPorId(
            @PathVariable Integer id
    ) {

        validarAcceso(PERMISO_VIEW);

        Optional<ReferenciaPersonal> ref =
                service.buscarPorId(id);

        return ref
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Listar referencias personales por persona.
     */
    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<List<ReferenciaPersonal>> listarPorPersona(
            @PathVariable Integer idDatosPersonal
    ) {

        validarAcceso(PERMISO_VIEW);

        List<ReferenciaPersonal> lista =
                service.listarPorPersona(idDatosPersonal);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    /**
     * Crear nueva referencia personal.
     */
    @PostMapping
    public ResponseEntity<ReferenciaPersonal> crear(
            @RequestBody ReferenciaPersonal nueva
    ) {

        validarAcceso(PERMISO_EDIT);

        ReferenciaPersonal guardada =
                service.crear(nueva);

        return ResponseEntity.ok(guardada);
    }

    /**
     * Actualizar referencia personal existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReferenciaPersonal> actualizar(
            @PathVariable Integer id,
            @RequestBody ReferenciaPersonal actualizada
    ) {

        validarAcceso(PERMISO_EDIT);

        return service.actualizar(id, actualizada)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Eliminar referencia personal.
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

        Usuario usuarioActual =
                new Usuario();

        usuarioActual.setIdUsuario(idUsuario);

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}