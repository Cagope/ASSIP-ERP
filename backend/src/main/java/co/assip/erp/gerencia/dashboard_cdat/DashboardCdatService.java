package co.assip.erp.gerencia.dashboard_cdat;

import co.assip.erp.gerencia.dashboard_cdat.dto.DashboardCdatRequestDTO;
import co.assip.erp.gerencia.dashboard_cdat.dto.DashboardCdatResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardCdatService {

    private final DashboardCdatRepository repository;

    public DashboardCdatResponseDTO consultar(
            DashboardCdatRequestDTO request
    ) {

        return DashboardCdatResponseDTO.builder()
                .resumen(
                        repository.obtenerResumen(request.getFechaCorte())
                )
                .agencias(
                        repository.obtenerAgencias(request.getFechaCorte())
                )
                .plazos(
                        repository.obtenerPlazos(request.getFechaCorte())
                )
                .tasas(
                        repository.obtenerTasas(request.getFechaCorte())
                )
                .tendencia(
                        repository.obtenerTendencia(request.getFechaCorte())
                )

                .vencimientos(
                        repository.obtenerVencimientos(request.getFechaCorte())
                )

                .build();

    }

}