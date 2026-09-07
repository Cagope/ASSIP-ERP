package co.assip.erp.gerencia.dashboard_cdat;

import co.assip.erp.gerencia.dashboard_cdat.dto.DashboardCdatResponseDTO;
import co.assip.erp.gerencia.dashboard_cdat.dto.DashboardCdatVencimientoDetalleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gerencia/dashboard-cdat")
@RequiredArgsConstructor
public class DashboardCdatController {

    private final DashboardCdatService service;

    // =========================================================
    // DASHBOARD ACTUAL
    // =========================================================

    @GetMapping("/consultar")
    public DashboardCdatResponseDTO consultar() {
        return service.consultar();
    }

    // =========================================================
    // DETALLE DE PRÓXIMOS VENCIMIENTOS
    // =========================================================

    @GetMapping("/vencimientos/detalle")
    public List<DashboardCdatVencimientoDetalleDTO>
    consultarVencimientosDetalle(
            @RequestParam String rango
    ) {
        return service.consultarVencimientosDetalle(rango);
    }
}