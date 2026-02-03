package co.assip.erp.activosfijos.informes;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/activos-fijos/informes")
@RequiredArgsConstructor
public class ActivosFijosInformesController {

    private final ActivosFijosInformesService service;

    // ============================================================
    // ✅ INFORME MAESTRO DE ACTIVOS FIJOS
    // GET /activos-fijos/informes/maestro-activos
    // ============================================================
    @GetMapping("/maestro-activos")
    public List<Map<String, Object>> maestroActivos() {
        return service.obtenerMaestroActivos();
    }

    // ============================================================
    // ✅ MOVIMIENTOS / KARDEX GENERAL DE ACTIVOS FIJOS
    // GET /activos-fijos/informes/movimientos-activos
    //
    // Filtros opcionales:
    // - idAgencia
    // - codigoMovimiento
    // - fechaIni (yyyy-MM-dd)
    // - fechaFin (yyyy-MM-dd)
    // - idActivoFijo
    // ============================================================
    @GetMapping("/movimientos-activos")
    public List<Map<String, Object>> movimientosActivos(
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) String codigoMovimiento,
            @RequestParam(required = false) String fechaIni,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) Long idActivoFijo
    ) {
        return service.obtenerMovimientosActivos(idAgencia, codigoMovimiento, fechaIni, fechaFin, idActivoFijo);
    }

    @GetMapping("/resumen-movimientos")
    public List<Map<String, Object>> resumenMovimientos(
            @RequestParam(required = false) Integer idAgencia,
            @RequestParam(required = false) String codigoMovimiento
    ) {
        return service.obtenerResumenMovimientos(idAgencia, codigoMovimiento);
    }

    @GetMapping("/activos-movimientos")
    public List<Map<String, Object>> activosMovimientos() {
        return service.obtenerActivosMovimientos();
    }

}
