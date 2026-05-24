package co.assip.erp.depositos.informes.saldos_menores;

import co.assip.erp.depositos.informes.saldos_menores.dto.SaldosMenoresItemDTO;
import co.assip.erp.depositos.informes.saldos_menores.dto.SaldosMenoresRequestDTO;
import co.assip.erp.depositos.informes.saldos_menores.dto.SaldosMenoresResponseDTO;
import co.assip.erp.depositos.informes.saldos_menores.dto.SaldosMenoresResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaldosMenoresService {

    private final SaldosMenoresRepository repository;

    public SaldosMenoresResponseDTO consultar(
            SaldosMenoresRequestDTO request
    ) {
        validar(request);

        List<SaldosMenoresItemDTO> items =
                repository.consultar(request);

        BigDecimal totalSaldos =
                items.stream()
                        .map(SaldosMenoresItemDTO::getSaldoCorte)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal saldoPromedio =
                items.isEmpty()
                        ? BigDecimal.ZERO
                        : totalSaldos.divide(
                        BigDecimal.valueOf(items.size()),
                        2,
                        RoundingMode.HALF_UP
                );

        SaldosMenoresResumenDTO resumen =
                SaldosMenoresResumenDTO.builder()
                        .totalCuentas(items.size())
                        .totalSaldos(totalSaldos)
                        .saldoPromedio(saldoPromedio)
                        .valorMaximo(request.getValorMaximo())
                        .build();

        return SaldosMenoresResponseDTO.builder()
                .resumen(resumen)
                .items(items)
                .build();
    }

    private void validar(
            SaldosMenoresRequestDTO request
    ) {
        if (request.getFechaCorte() == null) {
            throw new RuntimeException("La fecha de corte es obligatoria.");
        }

        if (request.getIdAgencia() == null || request.getIdAgencia() == 0) {
            throw new RuntimeException("Debe seleccionar una agencia.");
        }

        if (request.getValorMaximo() == null
                || request.getValorMaximo().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Debe indicar un valor válido.");
        }
    }

}