package co.assip.erp.depositos.informes.intereses_retencion;

import co.assip.erp.depositos.informes.intereses_retencion.dto.InteresesRetencionItemDTO;
import co.assip.erp.depositos.informes.intereses_retencion.dto.InteresesRetencionRequestDTO;
import co.assip.erp.depositos.informes.intereses_retencion.dto.InteresesRetencionResponseDTO;
import co.assip.erp.depositos.informes.intereses_retencion.dto.InteresesRetencionResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InteresesRetencionService {

    private final InteresesRetencionRepository repository;

    public InteresesRetencionResponseDTO consultar(
            InteresesRetencionRequestDTO request
    ) {

        validar(request);

        List<InteresesRetencionItemDTO> items =
                repository.consultar(request);

        InteresesRetencionResumenDTO resumen =
                construirResumen(items);

        return InteresesRetencionResponseDTO.builder()
                .resumen(resumen)
                .items(items)
                .build();
    }

    private void validar(
            InteresesRetencionRequestDTO request
    ) {

        if (request.getFechaInicial() == null || request.getFechaInicial().isBlank()) {
            throw new RuntimeException("Debe seleccionar la fecha inicial.");
        }

        if (request.getFechaFinal() == null || request.getFechaFinal().isBlank()) {
            throw new RuntimeException("Debe seleccionar la fecha final.");
        }
    }

    private InteresesRetencionResumenDTO construirResumen(
            List<InteresesRetencionItemDTO> items
    ) {

        BigDecimal totalIntereses =
                items.stream()
                        .map(x -> x.getIntereses() == null
                                ? BigDecimal.ZERO
                                : x.getIntereses())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRetencion =
                items.stream()
                        .map(x -> x.getRetencion() == null
                                ? BigDecimal.ZERO
                                : x.getRetencion())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNeto =
                totalIntereses.subtract(totalRetencion);

        return InteresesRetencionResumenDTO.builder()
                .totalCuentas(items.size())
                .totalIntereses(totalIntereses)
                .totalRetencion(totalRetencion)
                .totalNeto(totalNeto)
                .build();
    }

}