package co.assip.erp.shared.cuentas_ahorro.extracto;

import co.assip.erp.shared.cuentas_ahorro.extracto.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExtractoCuentaSharedService {

    private final ExtractoCuentaSharedRepository repository;

    public ExtractoCuentaSharedResponseDTO consultar(
            ExtractoCuentaSharedRequestDTO request
    ) {

        ExtractoCuentaSharedResumenDTO resumen =
                repository.obtenerResumen(request);

        ExtractoCuentaSharedEstadisticaDTO estadisticas =
                repository.obtenerEstadisticas(request);

        return ExtractoCuentaSharedResponseDTO.builder()
                .resumen(resumen)
                .estadisticas(estadisticas)
                .movimientos(
                        repository.obtenerMovimientos(
                                request,
                                resumen.getSaldoInicial()
                        )
                )
                .build();
    }
}