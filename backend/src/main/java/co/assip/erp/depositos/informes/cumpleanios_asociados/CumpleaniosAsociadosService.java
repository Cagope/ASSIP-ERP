package co.assip.erp.depositos.informes.cumpleanios_asociados;

import co.assip.erp.depositos.informes.cumpleanios_asociados.dto.CumpleaniosAsociadosItemDTO;
import co.assip.erp.depositos.informes.cumpleanios_asociados.dto.CumpleaniosAsociadosRequestDTO;
import co.assip.erp.depositos.informes.cumpleanios_asociados.dto.CumpleaniosAsociadosResponseDTO;
import co.assip.erp.depositos.informes.cumpleanios_asociados.dto.CumpleaniosAsociadosResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CumpleaniosAsociadosService {

    private final CumpleaniosAsociadosRepository repository;

    public CumpleaniosAsociadosResponseDTO consultar(
            CumpleaniosAsociadosRequestDTO request
    ) {

        validar(request);

        List<CumpleaniosAsociadosItemDTO> items =
                repository.consultar(request);

        CumpleaniosAsociadosResumenDTO resumen =
                CumpleaniosAsociadosResumenDTO.builder()
                        .totalAsociados(items.size())
                        .rangoFechas(
                                request.getFechaInicial()
                                        + " - "
                                        + request.getFechaFinal()
                        )
                        .build();

        return CumpleaniosAsociadosResponseDTO.builder()
                .resumen(resumen)
                .items(items)
                .build();
    }

    private void validar(
            CumpleaniosAsociadosRequestDTO request
    ) {

        if (request.getFechaInicial() == null) {
            throw new RuntimeException("La fecha inicial es obligatoria.");
        }

        if (request.getFechaFinal() == null) {
            throw new RuntimeException("La fecha final es obligatoria.");
        }

        if (request.getIdAgencia() == null
                || request.getIdAgencia() == 0) {
            throw new RuntimeException("Debe seleccionar una agencia.");
        }

        if (request.getFechaFinal()
                .isBefore(request.getFechaInicial())) {
            throw new RuntimeException("La fecha final no puede ser menor a la inicial.");
        }

    }

}