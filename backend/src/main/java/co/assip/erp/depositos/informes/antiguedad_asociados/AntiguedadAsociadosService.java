package co.assip.erp.depositos.informes.antiguedad_asociados;

import co.assip.erp.depositos.informes.antiguedad_asociados.dto.AntiguedadAsociadosItemDTO;
import co.assip.erp.depositos.informes.antiguedad_asociados.dto.AntiguedadAsociadosRequestDTO;
import co.assip.erp.depositos.informes.antiguedad_asociados.dto.AntiguedadAsociadosResponseDTO;
import co.assip.erp.depositos.informes.antiguedad_asociados.dto.AntiguedadAsociadosResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AntiguedadAsociadosService {

    private final AntiguedadAsociadosRepository repository;

    public AntiguedadAsociadosResponseDTO consultar(
            AntiguedadAsociadosRequestDTO request
    ) {
        validar(request);

        List<AntiguedadAsociadosItemDTO> items =
                repository.consultar(request);

        List<AntiguedadAsociadosResumenDTO> resumen =
                construirResumen(items);

        int limite =
                request.getLimitePantalla() == null
                        ? 20
                        : request.getLimitePantalla();

        List<AntiguedadAsociadosItemDTO> mayoresAntiguedad =
                items.stream()
                        .sorted(
                                Comparator.comparing(
                                        AntiguedadAsociadosItemDTO::getAniosAsociado,
                                        Comparator.nullsLast(Integer::compareTo)
                                ).reversed()
                        )
                        .limit(limite)
                        .toList();

        return AntiguedadAsociadosResponseDTO.builder()
                .resumen(resumen)
                .mayoresAntiguedad(mayoresAntiguedad)
                .itemsExcel(items)
                .build();
    }

    private List<AntiguedadAsociadosResumenDTO> construirResumen(
            List<AntiguedadAsociadosItemDTO> items
    ) {
        Map<String, List<AntiguedadAsociadosItemDTO>> grupos =
                items.stream()
                        .collect(
                                Collectors.groupingBy(
                                        AntiguedadAsociadosItemDTO::getRangoAntiguedad
                                )
                        );

        return grupos.entrySet()
                .stream()
                .map(entry -> {
                    List<AntiguedadAsociadosItemDTO> grupo =
                            entry.getValue();

                    BigDecimal saldoTotal =
                            grupo.stream()
                                    .map(AntiguedadAsociadosItemDTO::getSaldoAportes)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal saldoPromedio =
                            grupo.isEmpty()
                                    ? BigDecimal.ZERO
                                    : saldoTotal.divide(
                                    BigDecimal.valueOf(grupo.size()),
                                    2,
                                    RoundingMode.HALF_UP
                            );

                    BigDecimal edadPromedio =
                            calcularEdadPromedio(grupo);

                    return AntiguedadAsociadosResumenDTO.builder()
                            .rangoAntiguedad(entry.getKey())
                            .cantidadAsociados(grupo.size())
                            .saldoTotalAportes(saldoTotal)
                            .saldoPromedioAportes(saldoPromedio)
                            .edadPromedio(edadPromedio)
                            .build();
                })
                .sorted(
                        Comparator.comparing(
                                r -> ordenRango(r.getRangoAntiguedad())
                        )
                )
                .toList();
    }

    private BigDecimal calcularEdadPromedio(
            List<AntiguedadAsociadosItemDTO> grupo
    ) {
        List<Integer> edades =
                grupo.stream()
                        .map(AntiguedadAsociadosItemDTO::getEdad)
                        .filter(e -> e != null && e > 0)
                        .toList();

        if (edades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total =
                edades.stream()
                        .map(BigDecimal::valueOf)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(
                BigDecimal.valueOf(edades.size()),
                2,
                RoundingMode.HALF_UP
        );
    }

    private Integer ordenRango(
            String rango
    ) {
        return switch (rango) {
            case "0-1 años" -> 1;
            case "1-3 años" -> 2;
            case "3-5 años" -> 3;
            case "5-10 años" -> 4;
            case "10-15 años" -> 5;
            case "15-20 años" -> 6;
            default -> 7;
        };
    }

    private void validar(
            AntiguedadAsociadosRequestDTO request
    ) {
        if (request.getFechaCorte() == null) {
            throw new RuntimeException("La fecha de corte es obligatoria.");
        }

        if (request.getIdAgencia() == null || request.getIdAgencia() == 0) {
            throw new RuntimeException("Debe seleccionar una agencia.");
        }

        if (request.getLimitePantalla() != null
                && request.getLimitePantalla() > 100) {
            throw new RuntimeException("El límite de pantalla no puede ser mayor a 100.");
        }
    }

}