package co.assip.erp.depositos.informes.resumen_tipo_movimiento;

import co.assip.erp.depositos.informes.resumen_tipo_movimiento.dto.ResumenTipoMovimientoItemDTO;
import co.assip.erp.depositos.informes.resumen_tipo_movimiento.dto.ResumenTipoMovimientoRequestDTO;
import co.assip.erp.depositos.informes.resumen_tipo_movimiento.dto.ResumenTipoMovimientoResponseDTO;
import co.assip.erp.depositos.informes.resumen_tipo_movimiento.dto.ResumenTipoMovimientoResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResumenTipoMovimientoService {

    private final ResumenTipoMovimientoRepository repository;

    public ResumenTipoMovimientoResponseDTO consultar(
            ResumenTipoMovimientoRequestDTO request
    ) {

        validar(request);

        List<ResumenTipoMovimientoItemDTO> items =
                repository.consultar(request);

        ResumenTipoMovimientoResumenDTO resumen =
                construirResumen(items);

        return ResumenTipoMovimientoResponseDTO.builder()
                .resumen(resumen)
                .items(items)
                .build();
    }

    public ResumenTipoMovimientoResponseDTO detalle(
            ResumenTipoMovimientoRequestDTO request
    ) {

        validar(request);

        List<ResumenTipoMovimientoItemDTO> items =
                repository.detalle(request);

        ResumenTipoMovimientoResumenDTO resumen =
                construirResumenDetalle(items);

        return ResumenTipoMovimientoResponseDTO.builder()
                .resumen(resumen)
                .items(items)
                .build();
    }

    public List<Map<String, Object>> listarTiposMovimiento() {
        return repository.listarTiposMovimiento();
    }

    private void validar(
            ResumenTipoMovimientoRequestDTO request
    ) {

        if (request.getFechaInicial() == null || request.getFechaInicial().isBlank()) {
            throw new RuntimeException("Debe seleccionar la fecha inicial.");
        }

        if (request.getFechaFinal() == null || request.getFechaFinal().isBlank()) {
            throw new RuntimeException("Debe seleccionar la fecha final.");
        }
    }

    private ResumenTipoMovimientoResumenDTO construirResumen(
            List<ResumenTipoMovimientoItemDTO> items
    ) {

        BigDecimal totalDebitos =
                items.stream()
                        .map(x -> x.getTotalDebitos() == null ? BigDecimal.ZERO : x.getTotalDebitos())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCreditos =
                items.stream()
                        .map(x -> x.getTotalCreditos() == null ? BigDecimal.ZERO : x.getTotalCreditos())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNeto =
                totalCreditos.subtract(totalDebitos);

        Integer totalMovimientos =
                items.stream()
                        .map(x -> x.getCantidadMovimientos() == null ? 0 : x.getCantidadMovimientos())
                        .reduce(0, Integer::sum);

        return ResumenTipoMovimientoResumenDTO.builder()
                .totalTipos(items.size())
                .totalMovimientos(totalMovimientos)
                .totalDebitos(totalDebitos)
                .totalCreditos(totalCreditos)
                .totalNeto(totalNeto)
                .build();
    }

    private ResumenTipoMovimientoResumenDTO construirResumenDetalle(
            List<ResumenTipoMovimientoItemDTO> items
    ) {

        BigDecimal totalDebitos =
                items.stream()
                        .map(x -> x.getDebito() == null ? BigDecimal.ZERO : x.getDebito())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCreditos =
                items.stream()
                        .map(x -> x.getCredito() == null ? BigDecimal.ZERO : x.getCredito())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNeto =
                totalCreditos.subtract(totalDebitos);

        return ResumenTipoMovimientoResumenDTO.builder()
                .totalTipos(0)
                .totalMovimientos(items.size())
                .totalDebitos(totalDebitos)
                .totalCreditos(totalCreditos)
                .totalNeto(totalNeto)
                .build();
    }

}