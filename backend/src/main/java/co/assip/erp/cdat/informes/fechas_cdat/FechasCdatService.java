package co.assip.erp.cdat.informes.fechas_cdat;

import co.assip.erp.cdat.informes.fechas_cdat.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FechasCdatService {

    private final FechasCdatRepository repository;

    public FechasCdatResponseDTO consultar(
            FechasCdatRequestDTO request
    ) {

        List<FechasCdatItemDTO> resultados =
                repository.consultar(request);

        FechasCdatResumenDTO resumen =
                construirResumen(resultados);

        return FechasCdatResponseDTO.builder()
                .resumen(resumen)
                .resultados(resultados)
                .build();
    }

    private FechasCdatResumenDTO construirResumen(
            List<FechasCdatItemDTO> resultados
    ) {

        int cantidad = resultados.size();

        BigDecimal valorTotal = resultados.stream()
                .map(FechasCdatItemDTO::getValor)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal promedioTasa = BigDecimal.ZERO;

        BigDecimal promedioPlazo = BigDecimal.ZERO;

        if (!resultados.isEmpty()) {

            promedioTasa = resultados.stream()
                    .map(FechasCdatItemDTO::getTasa)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(
                            BigDecimal.valueOf(resultados.size()),
                            2,
                            RoundingMode.HALF_UP
                    );

            promedioPlazo = BigDecimal.valueOf(
                    resultados.stream()
                            .map(FechasCdatItemDTO::getPlazoMeses)
                            .filter(v -> v != null)
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(0)
            ).setScale(0, RoundingMode.HALF_UP);
        }

        return FechasCdatResumenDTO.builder()
                .cantidad(cantidad)
                .valorTotal(valorTotal)
                .promedioTasa(promedioTasa)
                .promedioPlazo(promedioPlazo)
                .build();
    }

}