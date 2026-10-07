package co.assip.erp.cartera.evaluacion.reglas;

import co.assip.erp.cartera.evaluacion.reglas.dto.EvaluacionCriterioReglaDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/evaluacion-criterios-reglas")
public class EvaluacionCriterioReglaController {

    private static final String PERMISO_VIEW =
            "CARTERA_EVALUACION_VIEW";

    private static final String PERMISO_EDIT =
            "CARTERA_EVALUACION_EDIT";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final EvaluacionCriterioReglaService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // LISTAR POR CRITERIO
    // =========================================================

    @GetMapping("/criterio/{idEvaluacionCriterio}")
    public List<EvaluacionCriterioReglaDTO> listarPorCriterio(
            @PathVariable Integer idEvaluacionCriterio
    ) {

        validarConsulta();

        return service.listarPorCriterio(
                idEvaluacionCriterio
        );
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    @GetMapping("/{idEvaluacionCriterioRegla}")
    public EvaluacionCriterioReglaDTO buscarPorId(
            @PathVariable Integer idEvaluacionCriterioRegla
    ) {

        validarConsulta();

        return service.buscarPorId(
                idEvaluacionCriterioRegla
        );
    }

    // =========================================================
    // CREAR
    // =========================================================

    @PostMapping
    public EvaluacionCriterioReglaDTO crear(
            @RequestBody EvaluacionCriterioReglaDTO dto
    ) {

        validarEdicion();

        return service.crear(dto);
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    @PutMapping("/{idEvaluacionCriterioRegla}")
    public EvaluacionCriterioReglaDTO actualizar(
            @PathVariable Integer idEvaluacionCriterioRegla,
            @RequestBody EvaluacionCriterioReglaDTO dto
    ) {

        validarEdicion();

        return service.actualizar(
                idEvaluacionCriterioRegla,
                dto
        );
    }

    // =========================================================
    // ACTIVAR / DESACTIVAR
    // =========================================================

    @PutMapping("/{idEvaluacionCriterioRegla}/activo/{activo}")
    public EvaluacionCriterioReglaDTO cambiarActivo(
            @PathVariable Integer idEvaluacionCriterioRegla,
            @PathVariable boolean activo
    ) {

        validarEdicion();

        return service.cambiarActivo(
                idEvaluacionCriterioRegla,
                activo
        );
    }

    // =========================================================
    // SEGURIDAD
    // =========================================================

    private void validarConsulta() {

        accessValidator.validarAcceso(
                usuarioActual(),
                PERMISO_VIEW
        );
    }

    private void validarEdicion() {

        accessValidator.validarAcceso(
                usuarioActual(),
                PERMISO_EDIT
        );
    }

    private Usuario usuarioActual() {

        Usuario usuarioActual = new Usuario();

        usuarioActual.setIdUsuario(
                usuarioSesionService.idUsuario()
        );

        return usuarioActual;
    }
}