package co.assip.erp.depositos.informes.movimientos_por_meses;

import co.assip.erp.depositos.informes.movimientos_por_meses.dto.MovimientosPorMesesAsociadoDTO;
import co.assip.erp.depositos.informes.movimientos_por_meses.dto.MovimientosPorMesesRequestDTO;
import co.assip.erp.depositos.informes.movimientos_por_meses.dto.MovimientosPorMesesResponseDTO;
import co.assip.erp.depositos.informes.movimientos_por_meses.dto.MovimientosPorMesesResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientosPorMesesService {

    private final MovimientosPorMesesRepository repository;

    public MovimientosPorMesesResponseDTO consultar(
            MovimientosPorMesesRequestDTO request
    ) {
        validar(request);

        List<String> meses = generarMeses(request);

        List<MovimientosPorMesesResumenDTO> resumen =
                repository.resumenMensual(request);

        List<MovimientosPorMesesAsociadoDTO> detalleAsociado =
                repository.detallePorAsociado(request);

        Integer totalMovimientos = resumen.stream()
                .map(MovimientosPorMesesResumenDTO::getCantidadMovimientos)
                .reduce(0, Integer::sum);

        BigDecimal totalEntradas = resumen.stream()
                .map(MovimientosPorMesesResumenDTO::getEntradas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSalidas = resumen.stream()
                .map(MovimientosPorMesesResumenDTO::getSalidas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return MovimientosPorMesesResponseDTO.builder()
                .meses(meses)
                .resumen(resumen)
                .detalleAsociado(detalleAsociado)
                .totalMovimientos(totalMovimientos)
                .totalEntradas(totalEntradas)
                .totalSalidas(totalSalidas)
                .build();
    }

    private void validar(
            MovimientosPorMesesRequestDTO request
    ) {
        if (request.getFechaCorte() == null) {
            throw new RuntimeException("La fecha de corte es obligatoria.");
        }

        if (request.getMeses() == null || request.getMeses() <= 0) {
            throw new RuntimeException("El número de meses debe ser mayor a cero.");
        }

        if (request.getMeses() > 36) {
            throw new RuntimeException("El número de meses no puede ser mayor a 36.");
        }
    }

    private List<String> generarMeses(
            MovimientosPorMesesRequestDTO request
    ) {
        List<String> meses = new ArrayList<>();

        YearMonth corte =
                YearMonth.from(request.getFechaCorte());

        for (int i = 0; i < request.getMeses(); i++) {
            meses.add(corte.minusMonths(i).toString());
        }

        return meses;
    }

}