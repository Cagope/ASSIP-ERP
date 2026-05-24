package co.assip.erp.depositos.cierre_mensual_depositos;

import co.assip.erp.depositos.cierre_mensual_depositos.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CierreMensualDepositosService {

    private final CierreMensualDepositosRepository repository;

    public CierreMensualDepositosPreviewDTO preview(
            CierreMensualDepositosRequestDTO request
    ) {

        validarRequest(request);
        validarUltimoDiaMes(request.getFechaCierre());

        List<CierreMensualDepositosDetalleDTO> detalle =
                repository.generarDetalle(
                        request.getIdAgencia(),
                        request.getFechaCierre()
                );

        List<CierreMensualDepositosResumenFormaDTO> resumenFormas =
                repository.generarResumenFormas(
                        detalle
                );

        CierreMensualDepositosResumenDTO resumen =
                repository.generarResumenGeneral(
                        detalle,
                        resumenFormas
                );

        return CierreMensualDepositosPreviewDTO.builder()
                .resumen(resumen)
                .resumenFormas(resumenFormas)
                .detalle(detalle)
                .build();
    }

    @Transactional
    public CierreMensualDepositosApplyResponseDTO aplicar(
            CierreMensualDepositosRequestDTO request
    ) {

        validarRequest(request);
        validarUltimoDiaMes(request.getFechaCierre());

        if (repository.existeCierre(
                request.getIdAgencia(),
                request.getFechaCierre()
        )) {
            throw new RuntimeException(
                    "Ya existe un cierre mensual para la agencia y fecha seleccionada."
            );
        }

        CierreMensualDepositosPreviewDTO preview =
                preview(request);

        if (preview.getDetalle() == null || preview.getDetalle().isEmpty()) {
            throw new RuntimeException(
                    "No hay información para aplicar el cierre mensual."
            );
        }

        Long idCierre =
                repository.crearCierre(
                        request,
                        preview.getResumen()
                );

        repository.guardarDetalle(
                idCierre,
                preview.getDetalle()
        );

        repository.guardarResumenFormas(
                idCierre,
                preview.getResumenFormas()
        );

        return CierreMensualDepositosApplyResponseDTO.builder()
                .idCierreMensual(idCierre)
                .mensaje("Cierre mensual de depósitos aplicado correctamente.")
                .build();
    }

    private void validarRequest(
            CierreMensualDepositosRequestDTO request
    ) {

        if (request == null) {
            throw new RuntimeException("Solicitud inválida.");
        }

        if (request.getIdAgencia() == null) {
            throw new RuntimeException("Debe seleccionar una agencia.");
        }

        if (request.getFechaCierre() == null) {
            throw new RuntimeException("Debe seleccionar la fecha de cierre.");
        }
    }

    private void validarUltimoDiaMes(
            LocalDate fecha
    ) {

        YearMonth ym =
                YearMonth.of(
                        fecha.getYear(),
                        fecha.getMonth()
                );

        if (!fecha.equals(ym.atEndOfMonth())) {
            throw new RuntimeException(
                    "La fecha de cierre debe ser el último día del mes."
            );
        }
    }

    public List<CierreMensualDepositosPreviewDTO> listar() {
        return repository.listar();
    }

    public CierreMensualDepositosPreviewDTO obtenerPorId(
            Long idCierre
    ) {
        return repository.obtenerPorId(idCierre);
    }

    @Transactional
    public void eliminar(
            Long idCierre
    ) {
        repository.eliminar(idCierre);
    }

}