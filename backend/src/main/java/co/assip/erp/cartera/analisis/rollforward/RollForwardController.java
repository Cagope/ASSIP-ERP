package co.assip.erp.cartera.analisis.rollforward;

import co.assip.erp.cartera.analisis.rollforward.dto.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/cartera/analisis/roll-forward")
public class RollForwardController {

    private final RollForwardService service;

    public RollForwardController(RollForwardService service) {
        this.service = service;
    }

    @GetMapping("/cortes")
    public List<RollForwardCorteDTO> listarCortes() {
        return service.listarCortes();
    }

    @GetMapping("/resumen")
    public RollForwardResumenDTO obtenerResumen(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte) {
        return service.obtenerResumen(fechaCorte);
    }

    @GetMapping("/resumen/historico")
    public List<RollForwardResumenDTO> listarHistoricoResumen() {
        return service.listarHistoricoResumen();
    }

    @GetMapping("/lineas")
    public List<RollForwardLineaDTO> listarLineas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte) {
        return service.listarLineas(fechaCorte);
    }

    @GetMapping("/tipos-movimiento")
    public List<String> listarTiposMovimiento(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte) {
        return service.listarTiposMovimiento(fechaCorte);
    }

    @GetMapping("/detalle")
    public List<RollForwardDetalleDTO> listarDetalle(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaCorte,
            @RequestParam(required = false) String tipoMovimiento,
            @RequestParam(required = false) Long idLineaCredito) {
        return service.listarDetalle(fechaCorte, tipoMovimiento, idLineaCredito);
    }
}
