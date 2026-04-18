package co.assip.erp.nomina.vacaciones;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.vacaciones.dto.VacacionPreviewDTO;
import co.assip.erp.nomina.vacaciones.dto.VacacionPreviewRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VacacionesService {

    private final VacacionesRepository repo;
    private final VacacionesCalculator calculator;

    public VacacionPreviewDTO preview(VacacionPreviewRequestDTO request) {

        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        if (request.getIdContrato() == null) {
            throw new IllegalArgumentException("El contrato es obligatorio");
        }

        if (request.getFechaLiquidacion() == null) {
            throw new IllegalArgumentException("La fecha de liquidación es obligatoria");
        }

        EmpleadoContratoDTO contrato = repo.obtenerContrato(request.getIdContrato());

        if (contrato == null) {
            throw new IllegalArgumentException("No se encontró el contrato");
        }

        if (!Boolean.TRUE.equals(contrato.getActivo())) {
            throw new IllegalStateException("El contrato no está activo");
        }

        return calculator.calcular(
                contrato,
                request.getFechaLiquidacion()
        );
    }
}