package co.assip.erp.activosfijos.localizaciones;

import co.assip.erp.activosfijos.localizaciones.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/activos-fijos/localizaciones")
public class LocalizacionController {

    private final LocalizacionService service;

    // =========================================================
    // LISTADO
    // =========================================================
    @GetMapping
    public List<LocalizacionListDTO> listar() {
        return service.listar();
    }

    // =========================================================
    // FORMULARIO
    // =========================================================
    @GetMapping("/{id}")
    public LocalizacionFormDTO obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    // =========================================================
    // CREAR
    // =========================================================
    @PostMapping
    public void crear(
            @RequestBody LocalizacionSaveDTO dto,
            @RequestHeader("X-Usuario-Id") Integer idUsuario
    ) {
        service.crear(dto, idUsuario);
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================
    @PutMapping("/{id}")
    public void actualizar(
            @PathVariable Long id,
            @RequestBody LocalizacionSaveDTO dto,
            @RequestHeader("X-Usuario-Id") Integer idUsuario
    ) {
        service.actualizar(id, dto, idUsuario);
    }

    // =========================================================
    // ELIMINAR
    // =========================================================
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
