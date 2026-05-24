package co.assip.erp.gerencia.dashboard_cdat;

import co.assip.erp.gerencia.dashboard_cdat.dto.DashboardCdatRequestDTO;
import co.assip.erp.gerencia.dashboard_cdat.dto.DashboardCdatResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gerencia/dashboard-cdat")
@RequiredArgsConstructor
public class DashboardCdatController {

    private final DashboardCdatService service;

    @PostMapping("/consultar")
    public DashboardCdatResponseDTO consultar(
            @RequestBody DashboardCdatRequestDTO request
    ) {
        return service.consultar(request);
    }

}