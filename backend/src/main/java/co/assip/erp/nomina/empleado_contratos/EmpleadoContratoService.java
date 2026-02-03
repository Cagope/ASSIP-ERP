package co.assip.erp.nomina.empleado_contratos;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpleadoContratoService {

    private final EmpleadoContratoRepository repository;

    public List<EmpleadoContratoDTO> listar() {
        return repository.listar();
    }

    public EmpleadoContratoDTO obtener(Integer idContrato) {
        return repository.obtener(idContrato)
                .orElseThrow(() -> new RuntimeException("No existe contrato con ID: " + idContrato));
    }

    public Integer crear(EmpleadoContratoDTO dto, Integer idUsuario) {
        validar(dto);
        return repository.crear(dto, idUsuario);
    }

    public void actualizar(Integer idContrato, EmpleadoContratoDTO dto, Integer idUsuario) {
        validar(dto);
        repository.actualizar(idContrato, dto, idUsuario);
    }

    public void eliminar(Integer idContrato) {
        repository.eliminar(idContrato);
    }

    private void validar(EmpleadoContratoDTO dto) {

        if (dto.getIdEmpleado() == null) {
            throw new RuntimeException("El empleado es obligatorio.");
        }

        if (dto.getFechaInicio() == null) {
            throw new RuntimeException("La fecha de inicio es obligatoria.");
        }

        if (dto.getIdTipoContrato() == null) {
            throw new RuntimeException("El tipo de contrato es obligatorio.");
        }

        if (dto.getPeriodoPago() == null || dto.getPeriodoPago().isBlank()) {
            dto.setPeriodoPago("MENSUAL");
        }

        if (dto.getSalarioBase() == null) {
            dto.setSalarioBase(java.math.BigDecimal.ZERO);
        }

        if (dto.getSalarioIntegral() == null) {
            dto.setSalarioIntegral(Boolean.FALSE);
        }

        if (dto.getClaseRiesgoArl() == null) {
            dto.setClaseRiesgoArl((short) 1);
        }

        if (dto.getPorcentajeArl() == null) {
            dto.setPorcentajeArl(java.math.BigDecimal.ZERO);
        }

        if (dto.getActivo() == null) {
            dto.setActivo(Boolean.TRUE);
        }
    }
}
