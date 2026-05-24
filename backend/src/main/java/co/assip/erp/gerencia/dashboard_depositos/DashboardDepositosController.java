package co.assip.erp.gerencia.dashboard_depositos;

import co.assip.erp.gerencia.dashboard_depositos.dto.DashboardDepositosRequestDTO;
import co.assip.erp.gerencia.dashboard_depositos.dto.DashboardDepositosResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gerencia/dashboard-depositos")
@RequiredArgsConstructor
public class DashboardDepositosController {

    private final DashboardDepositosService service;

    @PostMapping
    public DashboardDepositosResponseDTO consultar(
            @RequestBody DashboardDepositosRequestDTO request
    ) {
        return service.consultar(request);
    }

}