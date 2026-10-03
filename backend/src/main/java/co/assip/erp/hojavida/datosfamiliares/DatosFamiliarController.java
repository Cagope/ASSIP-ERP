package co.assip.erp.hojavida.datosfamiliares;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 🧭 Controlador REST — Gestión de Datos Familiares
 *
 * Endpoint base: /api/v1/hoja-vida/datos-familiares
 *
 * Seguridad:
 *  - GET                 → HOJAVIDA_PERSONA_VIEW
 *  - POST / PUT / DELETE → HOJAVIDA_PERSONA_EDIT
 */
@RestController
@RequestMapping("/hoja-vida/datos-familiares")
public class DatosFamiliarController {

    private static final String PERMISO_VIEW =
            "HOJAVIDA_PERSONA_VIEW";

    private static final String PERMISO_EDIT =
            "HOJAVIDA_PERSONA_EDIT";

    private final DatosFamiliarService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public DatosFamiliarController(
            DatosFamiliarService service,
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
    public List<DatosFamiliar> listar() {

        validarAcceso(PERMISO_VIEW);

        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatosFamiliar> buscarPorId(
            @PathVariable Integer id
    ) {

        validarAcceso(PERMISO_VIEW);

        Optional<DatosFamiliar> item =
                service.buscarPorId(id);

        return item
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/persona/{id}")
    public ResponseEntity<List<DatosFamiliar>> listarPorPersona(
            @PathVariable Integer id
    ) {

        validarAcceso(PERMISO_VIEW);

        List<DatosFamiliar> lista =
                service.listarPorPersona(id);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/parentesco/{codigo}")
    public ResponseEntity<List<DatosFamiliar>> listarPorParentesco(
            @PathVariable String codigo
    ) {

        validarAcceso(PERMISO_VIEW);

        List<DatosFamiliar> lista =
                service.listarPorParentesco(codigo);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/referencias")
    public ResponseEntity<List<DatosFamiliar>> listarReferencias() {

        validarAcceso(PERMISO_VIEW);

        List<DatosFamiliar> lista =
                service.listarReferencias();

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    // =========================================================
    // MODIFICACIONES
    // =========================================================

    @PostMapping
    public ResponseEntity<DatosFamiliar> crear(
            @RequestBody DatosFamiliar nuevo
    ) {

        validarAcceso(PERMISO_EDIT);

        DatosFamiliar guardado =
                service.crear(nuevo);

        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DatosFamiliar> actualizar(
            @PathVariable Integer id,
            @RequestBody DatosFamiliar actualizado
    ) {

        validarAcceso(PERMISO_EDIT);

        return service.actualizar(id, actualizado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

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