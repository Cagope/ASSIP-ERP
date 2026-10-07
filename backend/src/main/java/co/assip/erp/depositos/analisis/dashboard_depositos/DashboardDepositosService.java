package co.assip.erp.depositos.analisis.dashboard_depositos;

import co.assip.erp.depositos.analisis.dashboard_depositos.dto.DashboardDepositosRequestDTO;
import co.assip.erp.depositos.analisis.dashboard_depositos.dto.DashboardDepositosResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardDepositosService {

    private final DashboardDepositosRepository repository;

    public DashboardDepositosResponseDTO consultar(
            DashboardDepositosRequestDTO request
    ) {
        return repository.consultar(request);
    }

}