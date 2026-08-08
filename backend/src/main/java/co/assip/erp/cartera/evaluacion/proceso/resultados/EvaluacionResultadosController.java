package co.assip.erp.cartera.evaluacion.proceso.resultados;

import co.assip.erp.cartera.evaluacion.proceso.resultados.dto.EvaluacionResultadoCreditoDTO;
import co.assip.erp.cartera.evaluacion.proceso.resultados.dto.EvaluacionResultadoDetalleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import co.assip.erp.cartera.evaluacion.proceso.resultados.dto.EvaluacionResultadoHojaVidaDTO;
import co.assip.erp.cartera.evaluacion.proceso.resultados.dto.EvaluacionResultadoMorosidadDTO;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartera/evaluaciones/{idEvaluacionCartera}/resultados")
public class EvaluacionResultadosController {

    private final EvaluacionResultadosService service;


    // =========================================================
    // LISTAR RESULTADOS DE LA EVALUACIÓN
    //
    // Base para:
    // - pantalla de resultados;
    // - filtros;
    // - exportación general a Excel.
    // =========================================================

    @GetMapping
    public List<EvaluacionResultadoCreditoDTO> listarResultados(
            @PathVariable Integer idEvaluacionCartera
    ) {

        return service.listarResultados(
                idEvaluacionCartera
        );
    }


    // =========================================================
    // LISTAR DETALLE MASIVO POR ACCIÓN
    //
    // Base para impresión masiva:
    //
    // R = Reclasificar
    // H = Habilitar
    // M = Mantener
    //
    // IMPORTANTE:
    //
    // Este endpoint evita realizar una petición HTTP por cada
    // crédito evaluado.
    //
    // Ejemplo:
    //
    // /resultados/detalle/accion/M
    //
    // devuelve en una sola petición todos los criterios de
    // los créditos cuya acción sugerida sea M.
    // =========================================================

    @GetMapping("/detalle/accion/{accionEvaluacion}")
    public List<EvaluacionResultadoDetalleDTO> listarDetallePorAccion(
            @PathVariable Integer idEvaluacionCartera,
            @PathVariable String accionEvaluacion
    ) {

        return service.listarDetallePorAccion(
                idEvaluacionCartera,
                accionEvaluacion
        );
    }

    // =========================================================
    // LISTAR INSUMO DE HOJA DE VIDA
    //
    // Devuelve una fila por asociado incluido en la evaluación.
    //
    // La información corresponde a la fotografía definitiva
    // de Hoja de Vida utilizada para la misma fecha de corte.
    //
    // Base para:
    // - exportación completa a Excel;
    // - auditoría de los datos utilizados por el motor.
    // =========================================================

    @GetMapping("/insumos/hoja-vida")
    public List<EvaluacionResultadoHojaVidaDTO> listarFotoHojaVida(
            @PathVariable Integer idEvaluacionCartera
    ) {

        return service.listarFotoHojaVida(
                idEvaluacionCartera
        );
    }

    // =========================================================
// LISTAR INSUMO DE MOROSIDAD
//
// Devuelve los comprobantes consolidados utilizados como
// insumo para el criterio 401 - Servicio de la deuda.
//
// La información corresponde al último año contado desde
// la fecha de corte de la evaluación.
//
// Base para:
// - exportación completa a Excel;
// - auditoría de la morosidad utilizada por el motor.
// =========================================================

    @GetMapping("/insumos/morosidad")
    public List<EvaluacionResultadoMorosidadDTO> listarMorosidadExtracto(
            @PathVariable Integer idEvaluacionCartera
    ) {

        return service.listarMorosidadExtracto(
                idEvaluacionCartera
        );
    }

    // =========================================================
    // BUSCAR RESULTADO INDIVIDUAL
    //
    // Base para:
    // - consulta individual;
    // - impresión individual del crédito evaluado.
    // =========================================================

    @GetMapping("/{idEvaluacionCarteraCredito}")
    public EvaluacionResultadoCreditoDTO buscarResultadoPorId(
            @PathVariable Integer idEvaluacionCartera,
            @PathVariable Integer idEvaluacionCarteraCredito
    ) {

        return service.buscarResultadoPorId(
                idEvaluacionCartera,
                idEvaluacionCarteraCredito
        );
    }


    // =========================================================
    // LISTAR DETALLE DE CRITERIOS
    //
    // Devuelve los criterios aplicados al crédito evaluado.
    // =========================================================

    @GetMapping("/{idEvaluacionCarteraCredito}/detalle")
    public List<EvaluacionResultadoDetalleDTO> listarDetalle(
            @PathVariable Integer idEvaluacionCartera,
            @PathVariable Integer idEvaluacionCarteraCredito
    ) {

        return service.listarDetalle(
                idEvaluacionCartera,
                idEvaluacionCarteraCredito
        );
    }
}