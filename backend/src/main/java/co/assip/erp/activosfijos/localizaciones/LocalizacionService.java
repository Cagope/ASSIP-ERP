package co.assip.erp.activosfijos.localizaciones;

import co.assip.erp.activosfijos.localizaciones.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalizacionService {

    private final LocalizacionRepository repository;

    // =========================================================
    // LISTAR
    // =========================================================
    public List<LocalizacionListDTO> listar() {
        return repository.listar();
    }

    // =========================================================
    // FORMULARIO
    // =========================================================
    public LocalizacionFormDTO obtener(Long id) {
        return repository.obtener(id);
    }

    // =========================================================
    // CREAR
    // =========================================================
    @Transactional
    public void crear(LocalizacionSaveDTO dto, Integer idUsuario) {
        repository.insertar(dto, idUsuario);
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================
    @Transactional
    public void actualizar(Long id, LocalizacionSaveDTO dto, Integer idUsuario) {
        repository.actualizar(id, dto, idUsuario);
    }

    // =========================================================
    // ELIMINAR
    // =========================================================
    @Transactional
    public void eliminar(Long id) {
        repository.eliminar(id);
    }
}
