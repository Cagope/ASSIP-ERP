package co.assip.erp.gerencia.dashboard_cdat;

import co.assip.erp.gerencia.dashboard_cdat.dto.DashboardCdatResponseDTO;
import co.assip.erp.gerencia.dashboard_cdat.dto.DashboardCdatVencimientoDetalleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardCdatService {

    private static final Set<String> RANGOS_VENCIMIENTO = Set.of(
            "HOY",
            "EN 10 DIAS",
            "EN 20 DIAS",
            "EN 30 DIAS"
    );

    private final DashboardCdatRepository repository;

    // =========================================================
    // DASHBOARD ACTUAL
    // =========================================================

    public DashboardCdatResponseDTO consultar() {

        return DashboardCdatResponseDTO.builder()
                .resumen(
                        repository.obtenerResumen()
                )
                .agencias(
                        repository.obtenerAgencias()
                )
                .plazos(
                        repository.obtenerPlazos()
                )
                .tasas(
                        repository.obtenerTasas()
                )
                .vencimientos(
                        repository.obtenerVencimientos()
                )
                .tendencia(
                        repository.obtenerTendencia12Meses()
                )
                .build();
    }

    // =========================================================
    // DETALLE DE PRÓXIMOS VENCIMIENTOS
    // =========================================================

    public List<DashboardCdatVencimientoDetalleDTO>
    consultarVencimientosDetalle(String rango) {

        if (rango == null || rango.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe indicar el rango de vencimiento."
            );
        }

        String rangoNormalizado =
                rango.trim().toUpperCase();

        if (!RANGOS_VENCIMIENTO.contains(rangoNormalizado)) {
            throw new IllegalArgumentException(
                    "Rango de vencimiento no válido."
            );
        }

        return repository.obtenerVencimientosDetalle(
                rangoNormalizado
        );
    }
}