package co.assip.erp.cartera.evaluacion.criterios;

import co.assip.erp.cartera.evaluacion.criterios.dto.EvaluacionCriterioDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/evaluacion-criterios")
public class EvaluacionCriterioController {

    private static final String PERMISO_VIEW =
            "CARTERA_EVALUACION_VIEW";

    private static final String PERMISO_EDIT =
            "CARTERA_EVALUACION_EDIT";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final EvaluacionCriterioService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // LISTAR
    // =========================================================

    @GetMapping
    public List<EvaluacionCriterioDTO> listar() {

        validarConsulta();

        return service.listar();
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    @GetMapping("/{idEvaluacionCriterio}")
    public EvaluacionCriterioDTO buscarPorId(
            @PathVariable Integer idEvaluacionCriterio
    ) {

        validarConsulta();

        return service.buscarPorId(
                idEvaluacionCriterio
        );
    }

    // =========================================================
    // CREAR
    // =========================================================

    @PostMapping
    public EvaluacionCriterioDTO crear(
            @RequestBody EvaluacionCriterioDTO dto
    ) {

        validarEdicion();

        return service.crear(dto);
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    @PutMapping("/{idEvaluacionCriterio}")
    public EvaluacionCriterioDTO actualizar(
            @PathVariable Integer idEvaluacionCriterio,
            @RequestBody EvaluacionCriterioDTO dto
    ) {

        validarEdicion();

        return service.actualizar(
                idEvaluacionCriterio,
                dto
        );
    }

    // =========================================================
    // ACTIVAR / DESACTIVAR
    // =========================================================

    @PutMapping("/{idEvaluacionCriterio}/activo/{activo}")
    public EvaluacionCriterioDTO cambiarActivo(
            @PathVariable Integer idEvaluacionCriterio,
            @PathVariable boolean activo
    ) {

        validarEdicion();

        return service.cambiarActivo(
                idEvaluacionCriterio,
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