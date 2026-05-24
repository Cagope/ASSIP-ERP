package co.assip.erp.depositos.informes.promedios;

import co.assip.erp.depositos.informes.promedios.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromediosService {

    private final PromediosRepository repository;

    public PromediosResponseDTO consultar(
            PromediosRequestDTO request
    ) {

        validar(request);

        List<PromediosItemDTO> items =
                repository.consultarSaldos(request);

        BigDecimal totalSaldos =
                items.stream()
                        .map(PromediosItemDTO::getSaldoCorte)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal promedio =
                items.isEmpty()
                        ? BigDecimal.ZERO
                        : totalSaldos.divide(
                        BigDecimal.valueOf(items.size()),
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal saldoMayor =
                items.stream()
                        .map(PromediosItemDTO::getSaldoCorte)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

        BigDecimal saldoMenor =
                items.stream()
                        .map(PromediosItemDTO::getSaldoCorte)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

        PromediosResumenDTO resumen =
                PromediosResumenDTO.builder()
                        .totalCuentas(items.size())
                        .totalSaldos(totalSaldos)
                        .saldoPromedio(promedio)
                        .saldoMayor(saldoMayor)
                        .saldoMenor(saldoMenor)
                        .build();

        int limite =
                request.getLimitePantalla() == null
                        ? 20
                        : request.getLimitePantalla();

        List<PromediosItemDTO> mayores =
                items.stream()
                        .sorted(
                                Comparator.comparing(
                                        PromediosItemDTO::getSaldoCorte
                                ).reversed()
                        )
                        .limit(limite)
                        .toList();

        List<PromediosItemDTO> menores =
                items.stream()
                        .sorted(
                                Comparator.comparing(
                                        PromediosItemDTO::getSaldoCorte
                                )
                        )
                        .limit(limite)
                        .toList();

        return PromediosResponseDTO.builder()
                .resumen(resumen)
                .formas(repository.resumenPorForma(request))
                .mayores(mayores)
                .menores(menores)
                .itemsExcel(items)
                .build();
    }

    private void validar(
            PromediosRequestDTO request
    ) {

        if (request.getFechaCorte() == null) {
            throw new RuntimeException("La fecha de corte es obligatoria.");
        }

        if (request.getIdAgencia() == null
                || request.getIdAgencia() == 0) {
            throw new RuntimeException("Debe seleccionar una agencia.");
        }

    }

}