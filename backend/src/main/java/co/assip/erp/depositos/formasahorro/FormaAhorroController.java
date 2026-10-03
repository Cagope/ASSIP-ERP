package co.assip.erp.depositos.formasahorro;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos/formas-ahorro")
public class FormaAhorroController {

    private static final String PERMISO_VIEW =
            "DEPOSITOS_FORMAS_AHORRO_VIEW";

    private static final String PERMISO_EDIT =
            "DEPOSITOS_FORMAS_AHORRO_EDIT";

    private final FormaAhorroService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    public FormaAhorroController(
            FormaAhorroService service,
            UsuarioSesionService usuarioSesionService,
            AccessValidator accessValidator
    ) {
        this.service = service;
        this.usuarioSesionService = usuarioSesionService;
        this.accessValidator = accessValidator;
    }

    // ==========================================================
    // LISTAR TODAS
    // ==========================================================
    @GetMapping
    public List<FormaAhorro> listar() {

        validarAcceso(PERMISO_VIEW);

        return service.listar();
    }

    // ==========================================================
    // OBTENER UNA
    // ==========================================================
    @GetMapping("/{id}")
    public FormaAhorro obtener(@PathVariable Integer id) {

        validarAcceso(PERMISO_VIEW);

        return service.obtener(id);
    }

    // ==========================================================
    // CREAR
    // ==========================================================
    @PostMapping
    public FormaAhorro crear(@RequestBody FormaAhorro f) {

        validarAcceso(PERMISO_EDIT);

        return service.crear(f);
    }

    // ==========================================================
    // EDITAR
    // ==========================================================
    @PutMapping("/{id}")
    public FormaAhorro editar(
            @PathVariable Integer id,
            @RequestBody FormaAhorro f
    ) {

        validarAcceso(PERMISO_EDIT);

        return service.editar(id, f);
    }

    // ==========================================================
    // ELIMINAR
    // ==========================================================
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {

        validarAcceso(PERMISO_EDIT);

        service.eliminar(id);
    }

    // ==========================================================
    // SEGURIDAD
    // ==========================================================
    private void validarAcceso(String permiso) {

        Usuario usuarioActual = new Usuario();
        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        accessValidator.validarAcceso(
                usuarioActual,
                permiso
        );
    }
}