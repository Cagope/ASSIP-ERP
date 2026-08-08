package co.assip.erp.cartera.evaluacion.reglas;

import co.assip.erp.cartera.evaluacion.reglas.dto.EvaluacionCriterioReglaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/evaluacion-criterios-reglas")
public class EvaluacionCriterioReglaController {

    private final EvaluacionCriterioReglaService service;

    // =========================================================
    // LISTAR POR CRITERIO
    // =========================================================

    @GetMapping("/criterio/{idEvaluacionCriterio}")
    public List<EvaluacionCriterioReglaDTO> listarPorCriterio(
            @PathVariable Integer idEvaluacionCriterio
    ) {

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

        return service.actualizar(
                idEvaluacionCriterioRegla,
                dto
        );
    }

    // =========================================================
    // ACTIVAR / DESACTIVAR
    // =========================================================

    @PatchMapping("/{idEvaluacionCriterioRegla}/activo/{activo}")
    public EvaluacionCriterioReglaDTO cambiarActivo(
            @PathVariable Integer idEvaluacionCriterioRegla,
            @PathVariable boolean activo
    ) {

        return service.cambiarActivo(
                idEvaluacionCriterioRegla,
                activo
        );
    }
}