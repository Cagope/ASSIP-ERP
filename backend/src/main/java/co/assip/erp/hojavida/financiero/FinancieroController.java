package co.assip.erp.hojavida.financieros;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🌐 Controlador REST — Financieros
 * Esquema: hoja_vida
 *
 * Rutas:
 *  - GET    /api/v1/hoja-vida/financieros
 *  - GET    /api/v1/hoja-vida/financieros/{id}
 *  - GET    /api/v1/hoja-vida/financieros/persona/{idDatosPersonal}
 *  - POST   /api/v1/hoja-vida/financieros
 *  - PUT    /api/v1/hoja-vida/financieros/{id}
 *  - DELETE /api/v1/hoja-vida/financieros/{id}
 */
@RestController
@RequestMapping("/hoja-vida/financieros")
public class FinancieroController {

    private static final String PERMISO_VIEW =
            "HOJAVIDA_FINANCIERO_VIEW";

    private static final String PERMISO_EDIT =
            "HOJAVIDA_FINANCIERO_EDIT";

    private final FinancieroService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public FinancieroController(
            FinancieroService service,
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

    @GetMapping
    public ResponseEntity<List<Financiero>> listar() {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Financiero> obtener(
            @PathVariable Integer id
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<Financiero> obtenerPorPersona(
            @PathVariable Integer idDatosPersonal
    ) {

        validarAcceso(PERMISO_VIEW);

        return service.buscarPorPersona(idDatosPersonal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // MODIFICACIONES
    // =========================================================

    @PostMapping
    public ResponseEntity<Financiero> crear(
            @RequestBody Financiero f
    ) {

        validarAcceso(PERMISO_EDIT);

        try {
            return ResponseEntity.ok(service.crear(f));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Financiero> actualizar(
            @PathVariable Integer id,
            @RequestBody Financiero f
    ) {

        validarAcceso(PERMISO_EDIT);

        return service.actualizar(id, f)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer id
    ) {

        validarAcceso(PERMISO_EDIT);

        service.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarAcceso(String permiso) {

        Integer idUsuario = usuarioSesionService.idUsuario();

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(idUsuario);

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}