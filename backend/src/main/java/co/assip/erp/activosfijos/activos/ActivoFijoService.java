package co.assip.erp.activosfijos.activos;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.activosfijos.activos.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivoFijoService {

    private final ActivoFijoRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    // =========================================================
    // 1. LISTADO
    // =========================================================
    public List<ActivoFijoListDTO> listar() {
        return repository.listar();
    }

    // =========================================================
    // 2. FORMULARIO (DETALLE)
    // =========================================================
    public ActivoFijoFormDTO obtenerFormulario(Long idActivoFijo) {
        if (idActivoFijo == null || idActivoFijo <= 0) {
            throw new IllegalArgumentException("Activo fijo no válido.");
        }
        return repository.obtenerFormulario(idActivoFijo);
    }

    // =========================================================
    // 3. CREAR
    // =========================================================
    @Transactional
    public void crear(ActivoFijoSaveDTO dto) {

        validar(dto);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        var agencias =
                usuarioSesionService.agencias();

        // Si tiene una sola agencia, se fuerza (regla existente)
        if (agencias != null && agencias.size() == 1) {
            dto.setIdAgencia(agencias.get(0));
        }

        repository.insertar(dto, idUsuario);
    }

    // =========================================================
    // 4. ACTUALIZAR
    // =========================================================
    @Transactional
    public void actualizar(Long idActivoFijo, ActivoFijoSaveDTO dto) {

        if (idActivoFijo == null || idActivoFijo <= 0) {
            throw new IllegalArgumentException("Activo fijo no válido.");
        }

        validar(dto);

        // 🔹 Usuario desde contexto (NO obligatorio)
        Integer idUsuario =
                usuarioSesionService.idUsuario();

        repository.actualizar(idActivoFijo, dto, idUsuario);
    }

    // =========================================================
    // 5. ELIMINAR
    // =========================================================
    @Transactional
    public void eliminar(Long idActivoFijo) {

        if (idActivoFijo == null || idActivoFijo <= 0) {
            throw new IllegalArgumentException("Activo fijo no válido.");
        }

        repository.eliminar(idActivoFijo);
    }

    // =========================================================
    // VALIDACIONES DE NEGOCIO
    // =========================================================
    private void validar(ActivoFijoSaveDTO dto) {

        if (dto.getPlacaActivo() == null || dto.getPlacaActivo().isBlank()) {
            throw new IllegalArgumentException("La placa del activo es obligatoria.");
        }

        if (dto.getNombreActivo() == null || dto.getNombreActivo().isBlank()) {
            throw new IllegalArgumentException("El nombre del activo es obligatorio.");
        }

        if (dto.getFechaIngreso() == null) {
            throw new IllegalArgumentException("La fecha de ingreso es obligatoria.");
        }

        if (dto.getFechaGarantia() == null) {
            throw new IllegalArgumentException("La fecha de garantía es obligatoria.");
        }

        if (dto.getMesesDepreciacion() == null || dto.getMesesDepreciacion() < 0) {
            throw new IllegalArgumentException("Los meses de depreciación deben ser >= 0.");
        }

        if (dto.getValorAdquisicion() == null || dto.getValorAdquisicion().signum() <= 0) {
            throw new IllegalArgumentException("El valor de adquisición debe ser mayor a cero.");
        }

        if (dto.getValorMensual() == null || dto.getValorMensual().signum() < 0) {
            throw new IllegalArgumentException("El valor mensual no puede ser negativo.");
        }
    }
}
