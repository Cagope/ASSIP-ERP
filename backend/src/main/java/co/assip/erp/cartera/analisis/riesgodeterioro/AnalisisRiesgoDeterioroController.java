package co.assip.erp.cartera.analisis.riesgodeterioro;

import co.assip.erp.cartera.analisis.riesgodeterioro.dto.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(
        "/cartera/analisis/riesgo-deterioro"
)
public class AnalisisRiesgoDeterioroController {

    private final AnalisisRiesgoDeterioroService service;

    public AnalisisRiesgoDeterioroController(
            AnalisisRiesgoDeterioroService service
    ) {
        this.service = service;
    }


    // =========================================================
    // CORTES
    // =========================================================

    @GetMapping("/cortes")
    public List<LocalDate> listarCortes() {

        return service.listarCortes();
    }


    // =========================================================
    // RESUMEN
    // =========================================================

    @GetMapping("/resumen")
    public RiesgoDeterioroResumenDTO consultarResumen(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        return service.consultarResumen(
                fechaCorte,
                idAgencia,
                idLineaCredito
        );
    }


    // =========================================================
    // EDADES
    // =========================================================

    @GetMapping("/edades")
    public List<RiesgoDeterioroEdadDTO> consultarEdades(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaCorte,

            @RequestParam(
                    defaultValue = "CONTABLE"
            )
            String tipoEdad,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        return service.consultarEdades(
                fechaCorte,
                tipoEdad,
                idAgencia,
                idLineaCredito
        );
    }


    // =========================================================
    // SEGMENTACIÓN
    // =========================================================

    @GetMapping("/segmentacion")
    public List<RiesgoDeterioroSegmentoDTO> consultarSegmentacion(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaCorte,

            @RequestParam(
                    defaultValue = "LINEA"
            )
            String dimension,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        return service.consultarSegmentacion(
                fechaCorte,
                dimension,
                idAgencia,
                idLineaCredito
        );
    }


    // =========================================================
    // EVOLUCIÓN
    // =========================================================

    @GetMapping("/evolucion")
    public List<RiesgoDeterioroEvolucionDTO> consultarEvolucion(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaDesde,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaHasta,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        return service.consultarEvolucion(
                fechaDesde,
                fechaHasta,
                idAgencia,
                idLineaCredito
        );
    }


    // =========================================================
    // CONCENTRACIÓN
    // =========================================================

    @GetMapping("/concentracion")
    public List<RiesgoDeterioroConcentracionDTO> consultarConcentracion(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaCorte,

            @RequestParam(
                    defaultValue =
                            "PERDIDA_ESPERADA"
            )
            String criterio,

            @RequestParam(
                    defaultValue = "20"
            )
            Integer limite,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        return service.consultarConcentracion(
                fechaCorte,
                criterio,
                limite,
                idAgencia,
                idLineaCredito
        );
    }


    // =========================================================
    // DETALLE
    // =========================================================

    @GetMapping("/detalle")
    public List<RiesgoDeterioroDetalleDTO> consultarDetalle(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia,

            @RequestParam(required = false)
            Integer idLineaCredito
    ) {

        return service.consultarDetalle(
                fechaCorte,
                idAgencia,
                idLineaCredito
        );
    }
}