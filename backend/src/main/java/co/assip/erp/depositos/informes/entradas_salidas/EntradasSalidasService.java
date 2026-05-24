package co.assip.erp.depositos.informes.entradas_salidas;

import co.assip.erp.depositos.informes.entradas_salidas.dto.EntradasSalidasItemDTO;
import co.assip.erp.depositos.informes.entradas_salidas.dto.EntradasSalidasRequestDTO;
import co.assip.erp.depositos.informes.entradas_salidas.dto.EntradasSalidasResponseDTO;
import co.assip.erp.depositos.informes.entradas_salidas.dto.EntradasSalidasResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EntradasSalidasService {

    private final EntradasSalidasRepository repository;

    public EntradasSalidasResponseDTO consultar(
            EntradasSalidasRequestDTO request
    ) {

        validar(request);

        List<EntradasSalidasItemDTO> items =
                repository.consultar(request);

        EntradasSalidasResumenDTO resumen =
                construirResumen(items);

        return EntradasSalidasResponseDTO.builder()
                .resumen(resumen)
                .items(items)
                .build();
    }

    public EntradasSalidasResponseDTO resumenPorForma(
            EntradasSalidasRequestDTO request
    ) {

        validar(request);

        List<EntradasSalidasItemDTO> items =
                repository.resumenPorForma(request);

        EntradasSalidasResumenDTO resumen =
                construirResumen(items);

        return EntradasSalidasResponseDTO.builder()
                .resumen(resumen)
                .items(items)
                .build();
    }

    public EntradasSalidasResponseDTO detalleMovimientos(
            EntradasSalidasRequestDTO request
    ) {

        validar(request);

        List<EntradasSalidasItemDTO> items =
                repository.detalleMovimientos(request);

        EntradasSalidasResumenDTO resumen =
                construirResumenDetalle(items);

        return EntradasSalidasResponseDTO.builder()
                .resumen(resumen)
                .items(items)
                .build();
    }

    private void validar(
            EntradasSalidasRequestDTO request
    ) {

        if (request.getFechaInicial() == null || request.getFechaInicial().isBlank()) {
            throw new RuntimeException("Debe seleccionar la fecha inicial.");
        }

        if (request.getFechaFinal() == null || request.getFechaFinal().isBlank()) {
            throw new RuntimeException("Debe seleccionar la fecha final.");
        }
    }

    private EntradasSalidasResumenDTO construirResumen(
            List<EntradasSalidasItemDTO> items
    ) {

        BigDecimal totalEntradas =
                items.stream()
                        .map(EntradasSalidasItemDTO::getEntradas)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSalidas =
                items.stream()
                        .map(EntradasSalidasItemDTO::getSalidas)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNeto =
                totalEntradas.subtract(totalSalidas);

        Integer totalMovimientos =
                items.stream()
                        .map(EntradasSalidasItemDTO::getCantidadMovimientos)
                        .reduce(0, Integer::sum);

        return EntradasSalidasResumenDTO.builder()
                .totalDias(items.size())
                .totalMovimientos(totalMovimientos)
                .totalEntradas(totalEntradas)
                .totalSalidas(totalSalidas)
                .totalNeto(totalNeto)
                .build();
    }

    private EntradasSalidasResumenDTO construirResumenDetalle(
            List<EntradasSalidasItemDTO> items
    ) {

        BigDecimal totalEntradas =
                items.stream()
                        .map(x -> x.getCredito() == null ? BigDecimal.ZERO : x.getCredito())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSalidas =
                items.stream()
                        .map(x -> x.getDebito() == null ? BigDecimal.ZERO : x.getDebito())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNeto =
                totalEntradas.subtract(totalSalidas);

        return EntradasSalidasResumenDTO.builder()
                .totalDias(0)
                .totalMovimientos(items.size())
                .totalEntradas(totalEntradas)
                .totalSalidas(totalSalidas)
                .totalNeto(totalNeto)
                .build();
    }

}