package co.assip.erp.hojavida.sarlaft;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST — SARLAFT
 *
 * Endpoint base:
 * /api/v1/hoja-vida/sarlaft
 *
 * Seguridad:
 * - Consulta: HOJAVIDA_SARLAFT_VIEW
 * - Creación, actualización y eliminación:
 *   HOJAVIDA_SARLAFT_EDIT
 */
@RestController
@RequestMapping("/hoja-vida/sarlaft")
public class SarlaftController {

    private static final String PERMISO_VIEW =
            "HOJAVIDA_SARLAFT_VIEW";

    private static final String PERMISO_EDIT =
            "HOJAVIDA_SARLAFT_EDIT";

    private final SarlaftService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public SarlaftController(
            SarlaftService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    // ==========================================================
    // LISTAR TODOS
    // ==========================================================

    @GetMapping
    public ResponseEntity<List<Sarlaft>> listar() {

        validarAcceso(PERMISO_VIEW);

        List<Sarlaft> lista = service.listar();

        return ResponseEntity.ok(lista);
    }

    // ==========================================================
    // OBTENER POR ID
    // ==========================================================

    @GetMapping("/{id}")
    public ResponseEntity<Sarlaft> obtener(
            @PathVariable Integer id
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // OBTENER POR ID DE PERSONA
    // ==========================================================

    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<Sarlaft> obtenerPorPersona(
            @PathVariable Integer idDatosPersonal
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.buscarPorPersona(idDatosPersonal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // CREAR NUEVO REGISTRO
    // ==========================================================

    @PostMapping
    public ResponseEntity<Sarlaft> crear(
            @RequestBody Sarlaft sarlaft
    ) {

        validarAcceso(PERMISO_EDIT);

        try {
            Sarlaft creado =
                    service.crear(sarlaft);

            return ResponseEntity.ok(creado);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ==========================================================
    // ACTUALIZAR REGISTRO EXISTENTE
    // ==========================================================

    @PutMapping("/{id}")
    public ResponseEntity<Sarlaft> actualizar(
            @PathVariable Integer id,
            @RequestBody Sarlaft sarlaft
    ) {

        validarAcceso(PERMISO_EDIT);

        return service.actualizar(id, sarlaft)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // ELIMINAR REGISTRO
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
    // SEGURIDAD
    // ==========================================================

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