package co.assip.erp.depositos.informes.extracto_por_valor;

import co.assip.erp.depositos.informes.extracto_por_valor.dto.ExtractoPorValorItemDTO;
import co.assip.erp.depositos.informes.extracto_por_valor.dto.ExtractoPorValorRequestDTO;
import co.assip.erp.depositos.informes.extracto_por_valor.dto.ExtractoPorValorResponseDTO;
import co.assip.erp.depositos.informes.extracto_por_valor.dto.ExtractoPorValorResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtractoPorValorService {

    private final ExtractoPorValorRepository repository;

    public ExtractoPorValorResponseDTO consultar(
            ExtractoPorValorRequestDTO request
    ) {

        validar(request);

        List<ExtractoPorValorItemDTO> items =
                repository.consultar(request);

        ExtractoPorValorResumenDTO resumen =
                construirResumen(items);

        return ExtractoPorValorResponseDTO.builder()
                .resumen(resumen)
                .items(items)
                .build();
    }

    private void validar(
            ExtractoPorValorRequestDTO request
    ) {

        if (request.getFechaInicial() == null || request.getFechaInicial().isBlank()) {
            throw new RuntimeException("Debe seleccionar la fecha inicial.");
        }

        if (request.getFechaFinal() == null || request.getFechaFinal().isBlank()) {
            throw new RuntimeException("Debe seleccionar la fecha final.");
        }

        if (request.getValor() == null) {
            throw new RuntimeException("Debe indicar el valor a buscar.");
        }
    }

    private ExtractoPorValorResumenDTO construirResumen(
            List<ExtractoPorValorItemDTO> items
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

        return ExtractoPorValorResumenDTO.builder()
                .totalMovimientos(items.size())
                .totalDebitos(totalDebitos)
                .totalCreditos(totalCreditos)
                .totalNeto(totalNeto)
                .build();
    }

}