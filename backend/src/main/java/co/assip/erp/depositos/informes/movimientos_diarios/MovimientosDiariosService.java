package co.assip.erp.depositos.informes.movimientos_diarios;

import co.assip.erp.depositos.informes.movimientos_diarios.dto.MovimientosDiariosItemDTO;
import co.assip.erp.depositos.informes.movimientos_diarios.dto.MovimientosDiariosRequestDTO;
import co.assip.erp.depositos.informes.movimientos_diarios.dto.MovimientosDiariosResponseDTO;
import co.assip.erp.depositos.informes.movimientos_diarios.dto.MovimientosDiariosResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MovimientosDiariosService {

    private final MovimientosDiariosRepository repository;

    public MovimientosDiariosResponseDTO consultar(
            MovimientosDiariosRequestDTO request
    ) {

        validar(request);

        List<MovimientosDiariosItemDTO> items =
                repository.consultar(request);

        MovimientosDiariosResumenDTO resumen =
                construirResumen(items);

        return MovimientosDiariosResponseDTO.builder()
                .resumen(resumen)
                .items(items)
                .build();
    }

    private void validar(
            MovimientosDiariosRequestDTO request
    ) {

        if (request.getFechaInicial() == null || request.getFechaInicial().isBlank()) {
            throw new RuntimeException("Debe seleccionar la fecha inicial.");
        }

        if (request.getFechaFinal() == null || request.getFechaFinal().isBlank()) {
            throw new RuntimeException("Debe seleccionar la fecha final.");
        }
    }

    private MovimientosDiariosResumenDTO construirResumen(
            List<MovimientosDiariosItemDTO> items
    ) {

        BigDecimal totalDebitos =
                items.stream()
                        .map(x -> x.getDebito() == null ? BigDecimal.ZERO : x.getDebito())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCreditos =
                items.stream()
                        .map(x -> x.getCredito() == null ? BigDecimal.ZERO : x.getCredito())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return MovimientosDiariosResumenDTO.builder()
                .totalMovimientos(items.size())
                .totalDebitos(totalDebitos)
                .totalCreditos(totalCreditos)
                .build();
    }

    public List<Map<String, Object>> listarTiposMovimiento(
            String sql
    ) {
        return repository.listarTiposMovimiento(sql);
    }

}