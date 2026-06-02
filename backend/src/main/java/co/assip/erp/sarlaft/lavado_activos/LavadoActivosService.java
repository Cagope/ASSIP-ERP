package co.assip.erp.sarlaft.lavado_activos;

import co.assip.erp.sarlaft.lavado_activos.dto.LavadoActivosPreviewDTO;
import co.assip.erp.sarlaft.lavado_activos.dto.LavadoActivosRequestDTO;
import co.assip.erp.sarlaft.lavado_activos.dto.LavadoActivosResponseDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import co.assip.erp.sarlaft.lavado_activos.dto.LavadoActivosPersonaDTO;
import co.assip.erp.sarlaft.lavado_activos.dto.LavadoActivosFormatoDTO;

@Service
@RequiredArgsConstructor
public class LavadoActivosService {

    private final LavadoActivosRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public LavadoActivosPreviewDTO preview(
            Integer idAgencia,
            BigDecimal valorTransaccion
    ) {
        BigDecimal montoControl =
                repository.obtenerMontoControlLavado(
                        idAgencia
                );

        boolean requiere =
                valorTransaccion != null
                        && valorTransaccion.compareTo(montoControl) >= 0;

        return LavadoActivosPreviewDTO.builder()
                .idAgencia(idAgencia)
                .valorTransaccion(valorTransaccion)
                .montoControl(montoControl)
                .requiereFormato(requiere)
                .mensaje(
                        requiere
                                ? "La transacción requiere formato de lavado de activos."
                                : "La transacción no requiere formato de lavado de activos."
                )
                .build();
    }

    public boolean requiereControlLavado(
            Integer idAgencia,
            BigDecimal valorTransaccion
    ) {
        if (valorTransaccion == null) {
            return false;
        }

        BigDecimal montoControl =
                repository.obtenerMontoControlLavado(
                        idAgencia
                );

        return valorTransaccion.compareTo(montoControl) >= 0;
    }

    public LavadoActivosResponseDTO generarSiAplica(
            LavadoActivosRequestDTO request
    ) {
        validarRequest(request);

        boolean requiere =
                requiereControlLavado(
                        request.getIdAgencia(),
                        request.getValorTransaccion()
                );

        if (!requiere) {
            return LavadoActivosResponseDTO.builder()
                    .generado(false)
                    .requiereFormato(false)
                    .mensaje("La transacción no requiere formato de lavado de activos.")
                    .build();
        }

        if (
                request.getIdOrigen() != null
                        && repository.existeFormatoOrigen(
                        request.getModulo(),
                        request.getProceso(),
                        request.getIdOrigen()
                )
        ) {
            return LavadoActivosResponseDTO.builder()
                    .generado(false)
                    .requiereFormato(true)
                    .mensaje("Ya existe formato de lavado de activos para esta operación.")
                    .build();
        }

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Long id =
                repository.insertar(
                        request,
                        idUsuario
                );

        return LavadoActivosResponseDTO.builder()
                .idFormatoLavadoActivos(id)
                .generado(true)
                .requiereFormato(true)
                .mensaje("Se generó formato de lavado de activos pendiente de firma.")
                .build();
    }

    public void marcarImpreso(
            Long idLavadoActivos
    ) {
        repository.marcarImpreso(
                idLavadoActivos,
                usuarioSesionService.idUsuario()
        );
    }

    public void marcarFirmado(
            Long idLavadoActivos,
            String observacion
    ) {
        repository.marcarFirmado(
                idLavadoActivos,
                observacion,
                usuarioSesionService.idUsuario()
        );
    }

    private void validarRequest(
            LavadoActivosRequestDTO request
    ) {
        if (request == null) {
            throw new RuntimeException(
                    "No se recibió información para lavado de activos."
            );
        }

        if (request.getIdAgencia() == null) {
            throw new RuntimeException(
                    "La agencia es obligatoria para lavado de activos."
            );
        }

        if (request.getValorTransaccion() == null) {
            throw new RuntimeException(
                    "El valor de la transacción es obligatorio para lavado de activos."
            );
        }

        if (isBlank(request.getModulo())) {
            throw new RuntimeException(
                    "El módulo origen es obligatorio para lavado de activos."
            );
        }

        if (isBlank(request.getProceso())) {
            throw new RuntimeException(
                    "El proceso origen es obligatorio para lavado de activos."
            );
        }

        if (request.getFechaTransaccion() == null) {
            throw new RuntimeException(
                    "La fecha de transacción es obligatoria para lavado de activos."
            );
        }

        if (request.getFechaContable() == null) {
            throw new RuntimeException(
                    "La fecha contable es obligatoria para lavado de activos."
            );
        }

        if (isBlank(request.getDocumento())) {
            throw new RuntimeException(
                    "El documento del asociado es obligatorio para lavado de activos."
            );
        }

        if (isBlank(request.getNombreCompleto())) {
            throw new RuntimeException(
                    "El nombre del asociado es obligatorio para lavado de activos."
            );
        }
    }

    private boolean isBlank(
            String value
    ) {
        return value == null
                || value.trim().isEmpty();
    }

    public LavadoActivosPersonaDTO buscarPersonaPorDocumento(
            String documento
    ) {
        if (documento == null || documento.trim().isEmpty()) {
            throw new RuntimeException(
                    "El documento es obligatorio."
            );
        }

        return repository.buscarPersonaPorDocumento(
                documento.trim()
        );
    }

    public LavadoActivosFormatoDTO obtenerPorId(
            Long id
    ) {
        LavadoActivosFormatoDTO dto = repository.obtenerPorId(id);

        if (dto == null) {
            throw new RuntimeException(
                    "No existe el formato de lavado de activos solicitado."
            );
        }

        return dto;
    }
}