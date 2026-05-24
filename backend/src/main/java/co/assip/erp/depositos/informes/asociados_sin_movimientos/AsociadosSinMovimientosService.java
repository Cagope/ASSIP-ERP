package co.assip.erp.depositos.informes.asociados_sin_movimientos;

import co.assip.erp.depositos.informes.asociados_sin_movimientos.dto.AsociadosSinMovimientosItemDTO;
import co.assip.erp.depositos.informes.asociados_sin_movimientos.dto.AsociadosSinMovimientosRequestDTO;
import co.assip.erp.depositos.informes.asociados_sin_movimientos.dto.AsociadosSinMovimientosResponseDTO;
import co.assip.erp.depositos.informes.asociados_sin_movimientos.dto.AsociadosSinMovimientosResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AsociadosSinMovimientosService {

    private final AsociadosSinMovimientosRepository repository;

    public AsociadosSinMovimientosResponseDTO consultar(
            AsociadosSinMovimientosRequestDTO request
    ) {

        validar(request);

        List<AsociadosSinMovimientosItemDTO> items =
                repository.consultar(request);

        AsociadosSinMovimientosResumenDTO resumen =
                construirResumen(items);

        return AsociadosSinMovimientosResponseDTO.builder()
                .resumen(resumen)
                .items(items)
                .build();
    }

    private void validar(
            AsociadosSinMovimientosRequestDTO request
    ) {

        if (request.getFechaCorte() == null || request.getFechaCorte().isBlank()) {
            throw new RuntimeException("Debe seleccionar la fecha de corte.");
        }

        if (request.getDiasMinimos() == null || request.getDiasMinimos() < 0) {
            request.setDiasMinimos(90);
        }

        if (request.getTipoSaldo() == null) {
            request.setTipoSaldo(0);
        }

        if (request.getSaldoMinimo() == null) {
            request.setSaldoMinimo(BigDecimal.ZERO);
        }
    }

    private AsociadosSinMovimientosResumenDTO construirResumen(
            List<AsociadosSinMovimientosItemDTO> items
    ) {

        Integer totalConSaldo =
                (int) items.stream()
                        .filter(x -> x.getSaldoActual() != null
                                && x.getSaldoActual().compareTo(BigDecimal.ZERO) > 0)
                        .count();

        Integer totalSinSaldo =
                (int) items.stream()
                        .filter(x -> x.getSaldoActual() == null
                                || x.getSaldoActual().compareTo(BigDecimal.ZERO) == 0)
                        .count();

        BigDecimal saldoTotal =
                items.stream()
                        .map(x -> x.getSaldoActual() == null
                                ? BigDecimal.ZERO
                                : x.getSaldoActual())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AsociadosSinMovimientosResumenDTO.builder()
                .totalCuentas(items.size())
                .totalConSaldo(totalConSaldo)
                .totalSinSaldo(totalSinSaldo)
                .saldoTotal(saldoTotal)
                .build();
    }

}