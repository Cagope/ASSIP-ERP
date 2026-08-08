package co.assip.erp.cartera.evaluacion.criterios;

import co.assip.erp.cartera.evaluacion.criterios.dto.EvaluacionCriterioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/evaluacion-criterios")
public class EvaluacionCriterioController {

    private final EvaluacionCriterioService service;

    // =========================================================
    // LISTAR
    // =========================================================

    @GetMapping
    public List<EvaluacionCriterioDTO> listar() {
        return service.listar();
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    @GetMapping("/{idEvaluacionCriterio}")
    public EvaluacionCriterioDTO buscarPorId(
            @PathVariable Integer idEvaluacionCriterio
    ) {
        return service.buscarPorId(idEvaluacionCriterio);
    }

    // =========================================================
    // CREAR
    // =========================================================

    @PostMapping
    public EvaluacionCriterioDTO crear(
            @RequestBody EvaluacionCriterioDTO dto
    ) {
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
        return service.actualizar(
                idEvaluacionCriterio,
                dto
        );
    }

    // =========================================================
    // ACTIVAR / DESACTIVAR
    // =========================================================

    @PatchMapping("/{idEvaluacionCriterio}/activo/{activo}")
    public EvaluacionCriterioDTO cambiarActivo(
            @PathVariable Integer idEvaluacionCriterio,
            @PathVariable boolean activo
    ) {
        return service.cambiarActivo(
                idEvaluacionCriterio,
                activo
        );
    }
}