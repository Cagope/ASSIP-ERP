package co.assip.erp.cartera.evaluacion.proceso;

import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCarteraDTO;
import co.assip.erp.cartera.evaluacion.proceso.motor.dto.EvaluacionCreditoResultadoDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/evaluaciones")
public class EvaluacionCarteraController {

    private static final String PERMISO_VIEW =
            "CARTERA_EVALUACION_VIEW";

    private static final String PERMISO_EDIT =
            "CARTERA_EVALUACION_EDIT";

    // =========================================================
    // DEPENDENCIAS
    // =========================================================

    private final EvaluacionCarteraService service;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;

    // =========================================================
    // LISTAR
    // =========================================================

    @GetMapping
    public List<EvaluacionCarteraDTO> listar() {

        validarConsulta();

        return service.listar();
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    @GetMapping("/{idEvaluacionCartera}")
    public EvaluacionCarteraDTO buscarPorId(
            @PathVariable Integer idEvaluacionCartera
    ) {

        validarConsulta();

        return service.buscarPorId(
                idEvaluacionCartera
        );
    }

    // =========================================================
    // BUSCAR POR FECHA DE CORTE
    // =========================================================

    @GetMapping("/fecha-corte/{fechaCorte}")
    public EvaluacionCarteraDTO buscarPorFechaCorte(
            @PathVariable
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaCorte
    ) {

        validarConsulta();

        return service.buscarPorFechaCorte(
                fechaCorte
        );
    }

    // =========================================================
    // CREAR
    // =========================================================

    @PostMapping
    public EvaluacionCarteraDTO crear(
            @RequestBody EvaluacionCarteraDTO dto
    ) {

        validarEdicion();

        return service.crear(
                dto
        );
    }

    // =========================================================
    // ACTUALIZAR DATOS Y ACTAS
    // =========================================================

    @PutMapping("/{idEvaluacionCartera}")
    public EvaluacionCarteraDTO actualizar(
            @PathVariable Integer idEvaluacionCartera,
            @RequestBody EvaluacionCarteraDTO dto
    ) {

        validarEdicion();

        return service.actualizar(
                idEvaluacionCartera,
                dto
        );
    }

    // =========================================================
    // EJECUTAR EVALUACIÓN DE CARTERA
    // Guarda resultados definitivos mientras la evaluación
    // permanezca en estado P
    // =========================================================

    @PostMapping("/{idEvaluacionCartera}/ejecutar")
    public List<EvaluacionCreditoResultadoDTO> ejecutarEvaluacion(
            @PathVariable Integer idEvaluacionCartera
    ) {

        validarEdicion();

        return service.ejecutarEvaluacion(
                idEvaluacionCartera
        );
    }

    // =========================================================
    // MARCAR DEFINITIVA
    // =========================================================

    @PutMapping("/{idEvaluacionCartera}/definitiva")
    public EvaluacionCarteraDTO marcarDefinitiva(
            @PathVariable Integer idEvaluacionCartera
    ) {

        validarEdicion();

        return service.marcarDefinitiva(
                idEvaluacionCartera
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