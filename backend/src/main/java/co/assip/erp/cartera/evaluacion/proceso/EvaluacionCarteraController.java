package co.assip.erp.cartera.evaluacion.proceso;

import co.assip.erp.cartera.evaluacion.proceso.dto.EvaluacionCarteraDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import co.assip.erp.cartera.evaluacion.proceso.motor.dto.EvaluacionCreditoResultadoDTO;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/evaluaciones")
public class EvaluacionCarteraController {

    private final EvaluacionCarteraService service;

    // =========================================================
    // LISTAR
    // =========================================================

    @GetMapping
    public List<EvaluacionCarteraDTO> listar() {
        return service.listar();
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    @GetMapping("/{idEvaluacionCartera}")
    public EvaluacionCarteraDTO buscarPorId(
            @PathVariable Integer idEvaluacionCartera
    ) {

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
        return service.marcarDefinitiva(
                idEvaluacionCartera
        );
    }
}