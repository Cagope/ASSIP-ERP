package co.assip.erp.depositos.informes.saldos_rangos_edad;

import co.assip.erp.depositos.informes.saldos_rangos_edad.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaldosRangosEdadService {

    private final SaldosRangosEdadRepository repository;

    public SaldosRangosEdadResponseDTO consultar(
            SaldosRangosEdadRequestDTO request
    ) {
        validar(request);

        List<SaldosRangosEdadItemDTO> items =
                repository.consultar(request);

        List<SaldosRangosEdadResumenDTO> resumen =
                construirResumen(
                        request.getRangos(),
                        items
                );

        return SaldosRangosEdadResponseDTO.builder()
                .resumen(resumen)
                .itemsExcel(items)
                .build();
    }

    private List<SaldosRangosEdadResumenDTO> construirResumen(
            List<SaldosRangosEdadRangoDTO> rangos,
            List<SaldosRangosEdadItemDTO> items
    ) {
        List<SaldosRangosEdadResumenDTO> resumen =
                new ArrayList<>();

        for (SaldosRangosEdadRangoDTO rango : rangos) {

            List<SaldosRangosEdadItemDTO> grupo =
                    items.stream()
                            .filter(i ->
                                    rango.getNombreRango()
                                            .equals(i.getNombreRango())
                            )
                            .toList();

            BigDecimal saldoTotal =
                    grupo.stream()
                            .map(SaldosRangosEdadItemDTO::getSaldoAportes)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal salarioTotal =
                    grupo.stream()
                            .map(SaldosRangosEdadItemDTO::getSalario)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal saldoPromedio =
                    grupo.isEmpty()
                            ? BigDecimal.ZERO
                            : saldoTotal.divide(
                            BigDecimal.valueOf(grupo.size()),
                            2,
                            RoundingMode.HALF_UP
                    );

            BigDecimal salarioPromedio =
                    grupo.isEmpty()
                            ? BigDecimal.ZERO
                            : salarioTotal.divide(
                            BigDecimal.valueOf(grupo.size()),
                            2,
                            RoundingMode.HALF_UP
                    );

            resumen.add(
                    SaldosRangosEdadResumenDTO.builder()
                            .nombreRango(rango.getNombreRango())
                            .edadInicial(rango.getEdadInicial())
                            .edadFinal(rango.getEdadFinal())
                            .cantidadAsociados(grupo.size())
                            .saldoTotalAportes(saldoTotal)
                            .saldoPromedioAportes(saldoPromedio)
                            .salarioPromedio(salarioPromedio)
                            .build()
            );
        }

        return resumen;
    }

    private void validar(
            SaldosRangosEdadRequestDTO request
    ) {
        if (request.getFechaCorte() == null) {
            throw new RuntimeException("La fecha de corte es obligatoria.");
        }

        if (request.getIdAgencia() == null || request.getIdAgencia() == 0) {
            throw new RuntimeException("Debe seleccionar una agencia.");
        }

        if (request.getRangos() == null || request.getRangos().size() != 5) {
            throw new RuntimeException("Debe configurar exactamente 5 rangos de edad.");
        }

        for (SaldosRangosEdadRangoDTO rango : request.getRangos()) {

            if (rango.getNombreRango() == null
                    || rango.getNombreRango().isBlank()) {
                throw new RuntimeException("Todos los rangos deben tener nombre.");
            }

            if (rango.getEdadInicial() == null
                    || rango.getEdadFinal() == null) {
                throw new RuntimeException("Todos los rangos deben tener edad inicial y final.");
            }

            if (rango.getEdadInicial() < 0) {
                throw new RuntimeException("La edad inicial no puede ser negativa.");
            }

            if (rango.getEdadFinal() < rango.getEdadInicial()) {
                throw new RuntimeException("La edad final no puede ser menor que la edad inicial.");
            }
        }
    }

}