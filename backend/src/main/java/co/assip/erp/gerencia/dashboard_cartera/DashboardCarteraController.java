package co.assip.erp.gerencia.dashboard_cartera;

import co.assip.erp.gerencia.dashboard_cartera.dto.DashboardCarteraRequestDTO;
import co.assip.erp.gerencia.dashboard_cartera.dto.DashboardCarteraResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gerencia/dashboard-cartera")
public class DashboardCarteraController {

    private final DashboardCarteraService service;

    public DashboardCarteraController(
            DashboardCarteraService service
    ) {
        this.service = service;
    }

    // =========================================================
    // Consulta principal
    // =========================================================

    @PostMapping("/consultar")
    public ResponseEntity<DashboardCarteraResponseDTO> consultar(
            @RequestBody(required = false)
            DashboardCarteraRequestDTO request
    ) {

        return ResponseEntity.ok(
                service.consultar(request)
        );
    }
}