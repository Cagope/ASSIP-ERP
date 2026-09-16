package co.assip.erp.cdat.analisis.concentracion;

import co.assip.erp.cdat.analisis.concentracion.dto.ConcentracionCdatDepositanteDTO;
import co.assip.erp.cdat.analisis.concentracion.dto.ConcentracionCdatDetalleDTO;
import co.assip.erp.cdat.analisis.concentracion.dto.ConcentracionCdatResumenDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cdat/analisis/concentracion")
public class ConcentracionCdatController {

    private final ConcentracionCdatService service;

    public ConcentracionCdatController(
            ConcentracionCdatService service
    ) {
        this.service = service;
    }

    // =========================================================
    // CORTES DISPONIBLES
    // =========================================================

    @GetMapping("/cortes")
    public List<LocalDate> listarCortes() {
        return service.listarCortes();
    }

    // =========================================================
    // RESUMEN
    // =========================================================

    @GetMapping("/resumen")
    public ConcentracionCdatResumenDTO consultarResumen(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia
    ) {
        return service.consultarResumen(
                fechaCorte,
                idAgencia
        );
    }

    // =========================================================
    // RANKING DE DEPOSITANTES
    // =========================================================

    @GetMapping("/ranking")
    public List<ConcentracionCdatDepositanteDTO> consultarRanking(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia
    ) {
        return service.consultarRanking(
                fechaCorte,
                idAgencia
        );
    }

    // =========================================================
    // DETALLE DE CDAT POR DEPOSITANTE
    // =========================================================

    @GetMapping("/depositantes/{idDatosPersonal}/detalle")
    public List<ConcentracionCdatDetalleDTO> consultarDetalle(
            @PathVariable Long idDatosPersonal,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,

            @RequestParam(required = false)
            Integer idAgencia
    ) {
        return service.consultarDetalle(
                fechaCorte,
                idDatosPersonal,
                idAgencia
        );
    }
}