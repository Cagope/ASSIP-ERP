package co.assip.erp.depositos.informes.estadisticos_asociados;

import co.assip.erp.depositos.informes.estadisticos_asociados.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadisticosAsociadosService {

    private final EstadisticosAsociadosRepository repository;

    public EstadisticosAsociadosResponseDTO consultar(
            EstadisticosAsociadosRequestDTO request
    ) {

        validar(request);

        List<EstadisticosAsociadosItemDTO> items =
                repository.consultar(request);

        List<EstadisticosAsociadosDetalleDTO> detalle =
                repository.consultarDetalle(request);

        Integer totalAsociados =
                items.stream()
                        .map(EstadisticosAsociadosItemDTO::getCantidad)
                        .max(Integer::compareTo)
                        .orElse(0);

        BigDecimal totalAportes =
                items.stream()
                        .map(EstadisticosAsociadosItemDTO::getSaldoTotal)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

        List<EstadisticosAsociadosItemDTO> itemsFinal =
                items.stream()
                        .map(item ->
                                EstadisticosAsociadosItemDTO.builder()
                                        .grupo(item.getGrupo())
                                        .categoria(item.getCategoria())
                                        .cantidad(item.getCantidad())
                                        .saldoTotal(item.getSaldoTotal())
                                        .porcentaje(
                                                totalAsociados == 0
                                                        ? BigDecimal.ZERO
                                                        : BigDecimal.valueOf(item.getCantidad())
                                                        .multiply(BigDecimal.valueOf(100))
                                                        .divide(
                                                                BigDecimal.valueOf(totalAsociados),
                                                                2,
                                                                RoundingMode.HALF_UP
                                                        )
                                        )
                                        .build()
                        )
                        .toList();

        EstadisticosAsociadosResumenDTO resumen =
                EstadisticosAsociadosResumenDTO.builder()
                        .totalAsociados(totalAsociados)
                        .totalAportes(totalAportes)
                        .build();

        return EstadisticosAsociadosResponseDTO.builder()
                .resumen(resumen)
                .items(itemsFinal)
                .detalle(detalle)
                .build();

    }

    private void validar(
            EstadisticosAsociadosRequestDTO request
    ) {

        if (request.getFechaCorte() == null) {
            throw new RuntimeException("La fecha de corte es obligatoria.");
        }

        if (request.getIdAgencia() == null) {
            throw new RuntimeException("Debe seleccionar una agencia.");
        }

        if (request.getCodigoForma() == null
                || request.getCodigoForma().isBlank()) {
            throw new RuntimeException("Debe seleccionar una forma de ahorro.");
        }

    }

}