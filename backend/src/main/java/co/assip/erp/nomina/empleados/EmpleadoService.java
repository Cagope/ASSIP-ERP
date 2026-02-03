package co.assip.erp.nomina.empleados;

import co.assip.erp.nomina.empleados.dto.EmpleadoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpleadoService {

    private final EmpleadoRepository repository;

    public List<EmpleadoDTO> listar() {
        return repository.listar();
    }

    public EmpleadoDTO obtener(Integer idEmpleado) {
        return repository.obtener(idEmpleado)
                .orElseThrow(() -> new RuntimeException("No existe empleado nómina con ID: " + idEmpleado));
    }

    public Integer crear(EmpleadoDTO dto, Integer idUsuario) {
        validar(dto);
        return repository.crear(dto, idUsuario);
    }

    public void actualizar(Integer idEmpleado, EmpleadoDTO dto, Integer idUsuario) {
        validar(dto);
        repository.actualizar(idEmpleado, dto, idUsuario);
    }

    public void eliminar(Integer idEmpleado) {
        repository.eliminar(idEmpleado);
    }

    // ============================================================
    // ✅ VALIDACIONES MÍNIMAS
    // ============================================================
    private void validar(EmpleadoDTO dto) {
        if (dto.getIdAgencia() == null) {
            throw new RuntimeException("La agencia es obligatoria.");
        }
        if (dto.getIdDatosPersonal() == null) {
            throw new RuntimeException("La persona (datos personal) es obligatoria.");
        }
    }
}
