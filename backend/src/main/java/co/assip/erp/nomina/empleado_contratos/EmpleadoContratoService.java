package co.assip.erp.nomina.empleado_contratos;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoListViewDTO;


import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpleadoContratoService {

    private final EmpleadoContratoRepository repository;

    // ============================================================
    // LISTAR
    // ============================================================
    public List<EmpleadoContratoListViewDTO> listar() {
        return repository.listar();
    }

    // ============================================================
    // LISTAR POR EMPLEADO
    // ============================================================
    public List<EmpleadoContratoListViewDTO> listarPorEmpleado(Integer idEmpleado) {

        if (idEmpleado == null) {
            throw new IllegalArgumentException("El idEmpleado es obligatorio.");
        }

        return repository.listarPorEmpleado(idEmpleado);
    }

    // ============================================================
    // OBTENER
    // ============================================================
    public EmpleadoContratoDTO obtener(Integer idContrato) {
        return repository.obtener(idContrato)
                .orElseThrow(() ->
                        new IllegalArgumentException("No existe contrato con ID: " + idContrato));
    }

    // ============================================================
    // CREAR
    // ============================================================
    public Integer crear(EmpleadoContratoDTO dto, Integer idUsuario) {
        validar(dto);
        return repository.crear(dto, idUsuario);
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    public void actualizar(Integer idContrato, EmpleadoContratoDTO dto, Integer idUsuario) {
        validar(dto);
        repository.actualizar(idContrato, dto, idUsuario);
    }

    // ============================================================
    // ELIMINAR
    // ============================================================
    public void eliminar(Integer idContrato) {
        repository.eliminar(idContrato);
    }

    // ============================================================
    // VALIDACIÓN
    // ============================================================
    private void validar(EmpleadoContratoDTO dto) {

        if (dto.getIdEmpleado() == null) {
            throw new IllegalArgumentException("El empleado es obligatorio.");
        }

        if (dto.getFechaInicio() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria.");
        }

        if (dto.getIdTipoContrato() == null) {
            throw new IllegalArgumentException("El tipo de contrato es obligatorio.");
        }

        // 🔹 regla básica: fechaFin >= fechaInicio
        if (dto.getFechaFin() != null &&
                dto.getFechaFin().isBefore(dto.getFechaInicio())) {

            throw new IllegalArgumentException(
                    "La fecha fin no puede ser menor que la fecha inicio.");
        }

        if (dto.getPeriodoPago() == null || dto.getPeriodoPago().isBlank()) {
            dto.setPeriodoPago("MENSUAL");
        }

        if (dto.getSalarioBase() == null) {
            dto.setSalarioBase(BigDecimal.ZERO);
        }

        if (dto.getSalarioIntegral() == null) {
            dto.setSalarioIntegral(Boolean.FALSE);
        }

        if (dto.getClaseRiesgoArl() == null) {
            dto.setClaseRiesgoArl((short) 1);
        }

        if (dto.getPorcentajeArl() == null) {
            dto.setPorcentajeArl(BigDecimal.ZERO);
        }

        if (dto.getActivo() == null) {
            dto.setActivo(Boolean.TRUE);
        }
    }
}
