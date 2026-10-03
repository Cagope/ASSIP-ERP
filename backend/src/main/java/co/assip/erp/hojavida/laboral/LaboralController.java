package co.assip.erp.hojavida.laboral;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🌐 Controlador REST — Laboral
 * Esquema: hoja_vida
 *
 * Seguridad:
 *  - GET                 → HOJAVIDA_PERSONA_VIEW
 *  - POST / PUT / DELETE → HOJAVIDA_PERSONA_EDIT
 */
@RestController
@RequestMapping("/hoja-vida/laborales")
public class LaboralController {

    private static final String PERMISO_VIEW =
            "HOJAVIDA_PERSONA_VIEW";

    private static final String PERMISO_EDIT =
            "HOJAVIDA_PERSONA_EDIT";

    private final LaboralService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public LaboralController(
            LaboralService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    // ==========================================================
    // 🔹 LISTAR TODOS
    // ==========================================================

    @GetMapping
    public ResponseEntity<List<Laboral>> listar() {

        validarAcceso(PERMISO_VIEW);

        List<Laboral> lista = service.listar();

        return ResponseEntity.ok(lista);
    }

    // ==========================================================
    // 🔹 OBTENER POR ID
    // ==========================================================

    @GetMapping("/{id}")
    public ResponseEntity<Laboral> obtener(
            @PathVariable Integer id
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // 🔹 OBTENER POR ID DE PERSONA
    // ==========================================================

    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<Laboral> obtenerPorPersona(
            @PathVariable Integer idDatosPersonal
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.buscarPorPersona(idDatosPersonal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // 🔹 CREAR NUEVO REGISTRO
    // ==========================================================

    @PostMapping
    public ResponseEntity<Laboral> crear(
            @RequestBody Laboral laboral
    ) {

        validarAcceso(PERMISO_EDIT);

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
    public ResponseEntity<Laboral> actualizar(
            @PathVariable Integer id,
            @RequestBody Laboral laboral
    ) {

        validarAcceso(PERMISO_EDIT);

        return service.actualizar(id, laboral)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // 🔹 ELIMINAR REGISTRO
    // ==========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer id
    ) {

        validarAcceso(PERMISO_EDIT);

        service.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    // ==========================================================
    // 🔐 SEGURIDAD
    // ==========================================================

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