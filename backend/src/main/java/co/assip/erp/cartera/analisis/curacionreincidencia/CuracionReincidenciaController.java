package co.assip.erp.cartera.analisis.curacionreincidencia;

import co.assip.erp.cartera.analisis.curacionreincidencia.dto.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cartera/analisis/curacion-reincidencia")
public class CuracionReincidenciaController {

    private final CuracionReincidenciaService service;

    public CuracionReincidenciaController(CuracionReincidenciaService service) {
        this.service = service;
    }

    @GetMapping("/control")
    public CuracionReincidenciaControlDTO control() {
        return service.control();
    }

    @GetMapping("/resumen")
    public CuracionReincidenciaResumenDTO resumen(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String edadEntrada
    ) {
        return service.resumen(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    @GetMapping("/periodos")
    public List<CuracionReincidenciaPeriodoDTO> periodos(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String edadEntrada
    ) {
        return service.periodos(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    @GetMapping("/agencias")
    public List<CuracionReincidenciaSegmentoDTO> agencias(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String edadEntrada
    ) {
        return service.agencias(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    @GetMapping("/lineas")
    public List<CuracionReincidenciaSegmentoDTO> lineas(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String edadEntrada
    ) {
        return service.lineas(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    @GetMapping("/edades-entrada")
    public List<CuracionReincidenciaEdadEntradaDTO> edadesEntrada(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String edadEntrada
    ) {
        return service.edadesEntrada(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    @GetMapping("/distribucion-cura")
    public List<CuracionReincidenciaDistribucionCuraDTO> distribucionCura(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String edadEntrada
    ) {
        return service.distribucionCura(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    @GetMapping("/primera-reincidencia")
    public List<CuracionReincidenciaPrimeraReincidenciaDTO> primeraReincidencia(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String edadEntrada
    ) {
        return service.primeraReincidencia(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada
        );
    }

    @GetMapping("/detalle")
    public List<CuracionReincidenciaDetalleDTO> detalle(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoDesde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate periodoHasta,

            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) Integer idLineaCredito,
            @RequestParam(required = false) String edadEntrada,

            @RequestParam(defaultValue = "TODOS") String indicador
    ) {
        return service.detalle(
                periodoDesde,
                periodoHasta,
                idAgencia,
                idLineaCredito,
                edadEntrada,
                indicador
        );
    }
}
