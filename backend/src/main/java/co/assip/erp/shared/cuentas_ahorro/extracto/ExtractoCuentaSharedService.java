package co.assip.erp.shared.cuentas_ahorro.extracto;

import co.assip.erp.shared.cuentas_ahorro.extracto.dto.ExtractoCuentaSharedEstadisticaDTO;
import co.assip.erp.shared.cuentas_ahorro.extracto.dto.ExtractoCuentaSharedRequestDTO;
import co.assip.erp.shared.cuentas_ahorro.extracto.dto.ExtractoCuentaSharedResponseDTO;
import co.assip.erp.shared.cuentas_ahorro.extracto.dto.ExtractoCuentaSharedResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ExtractoCuentaSharedService {

    private final ExtractoCuentaSharedRepository repository;

    public ExtractoCuentaSharedResponseDTO consultar(
            ExtractoCuentaSharedRequestDTO request
    ) {

        validarRequest(request);

        ExtractoCuentaSharedResumenDTO resumen =
                repository.obtenerResumen(request);

        if (resumen == null) {
            throw new RuntimeException(
                    "La cuenta de ahorro no existe o no tiene información disponible."
            );
        }

        ExtractoCuentaSharedEstadisticaDTO estadisticas =
                repository.obtenerEstadisticas(request);

        BigDecimal saldoInicial =
                resumen.getSaldoInicial() == null
                        ? BigDecimal.ZERO
                        : resumen.getSaldoInicial();

        return ExtractoCuentaSharedResponseDTO.builder()
                .resumen(resumen)
                .estadisticas(estadisticas)
                .movimientos(
                        repository.obtenerMovimientos(
                                request,
                                saldoInicial
                        )
                )
                .build();
    }

    private void validarRequest(
            ExtractoCuentaSharedRequestDTO request
    ) {

        if (request == null) {
            throw new RuntimeException(
                    "No se recibió información para consultar el extracto."
            );
        }

        if (request.getIdCuentaAhorro() == null) {
            throw new RuntimeException(
                    "La cuenta de ahorro es obligatoria."
            );
        }

        if (request.getFechaInicial() == null) {
            throw new RuntimeException(
                    "La fecha inicial es obligatoria."
            );
        }

        if (request.getFechaFinal() == null) {
            throw new RuntimeException(
                    "La fecha final es obligatoria."
            );
        }

        if (
                request.getFechaInicial()
                        .compareTo(request.getFechaFinal()) > 0
        ) {
            throw new RuntimeException(
                    "La fecha inicial no puede ser mayor que la fecha final."
            );
        }
    }
}