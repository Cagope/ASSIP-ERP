package co.assip.erp.hojavida.residencia_fiscal;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST — Residencia Fiscal
 *
 * Endpoint base:
 * /api/v1/hoja-vida/residencia-fiscal
 *
 * Seguridad:
 * - Consulta: HOJAVIDA_FISCAL_VIEW
 * - Creación, actualización y eliminación:
 *   HOJAVIDA_FISCAL_EDIT
 */
@RestController
@RequestMapping("/hoja-vida/residencia-fiscal")
public class ResidenciaFiscalController {

    private static final String PERMISO_VIEW =
            "HOJAVIDA_FISCAL_VIEW";

    private static final String PERMISO_EDIT =
            "HOJAVIDA_FISCAL_EDIT";

    private final ResidenciaFiscalService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public ResidenciaFiscalController(
            ResidenciaFiscalService service,
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
    public ResponseEntity<List<ResidenciaFiscal>> listar() {

        validarAcceso(PERMISO_VIEW);

        return ResponseEntity.ok(
                service.listar()
        );
    }

    // ==========================================================
    // OBTENER POR ID
    // ==========================================================

    @GetMapping("/{id}")
    public ResponseEntity<ResidenciaFiscal> obtener(
            @PathVariable Long id
    ) {

        validarAcceso(PERMISO_VIEW);

        return service
                .buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // OBTENER POR PERSONA
    // ==========================================================

    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<ResidenciaFiscal> obtenerPorPersona(
            @PathVariable Integer idDatosPersonal
    ) {

        validarAcceso(PERMISO_VIEW);

        return service
                .buscarPorPersona(idDatosPersonal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // CREAR
    // ==========================================================

    @PostMapping
    public ResponseEntity<ResidenciaFiscal> crear(
            @RequestBody ResidenciaFiscal residenciaFiscal
    ) {

        validarAcceso(PERMISO_EDIT);

        return ResponseEntity.ok(
                service.crear(residenciaFiscal)
        );
    }

    // ==========================================================
    // ACTUALIZAR
    // ==========================================================

    @PutMapping("/{id}")
    public ResponseEntity<ResidenciaFiscal> actualizar(
            @PathVariable Long id,
            @RequestBody ResidenciaFiscal residenciaFiscal
    ) {

        validarAcceso(PERMISO_EDIT);

        return service
                .actualizar(id, residenciaFiscal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================================
    // ELIMINAR
    // ==========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {

        validarAcceso(PERMISO_EDIT);

        if (!service.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }

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