package co.assip.erp.activosfijos.activos;

import co.assip.erp.activosfijos.activos.dto.ActivoFijoFormDTO;
import co.assip.erp.activosfijos.activos.dto.ActivoFijoListDTO;
import co.assip.erp.activosfijos.activos.dto.ActivoFijoSaveDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/activos-fijos/activos")
public class ActivoFijoController {

    private final ActivoFijoService service;

    // =========================================================
    // 1. LISTADO
    // =========================================================
    @GetMapping
    public List<ActivoFijoListDTO> listar() {
        System.out.println("🔥 Listando activos fijos");
        return service.listar();
    }

    // =========================================================
    // 2. FORMULARIO (detalle completo)
    // =========================================================
    @GetMapping("/{id}/form")
    public ActivoFijoFormDTO obtenerFormulario(@PathVariable Long id) {
        System.out.println("🔥 Cargando formulario activo fijo id=" + id);
        return service.obtenerFormulario(id);
    }

    // =========================================================
    // 3. CREAR
    // =========================================================
    @PostMapping
    public void crear(@RequestBody ActivoFijoSaveDTO dto) {
        System.out.println("🔥 Crear activo fijo");
        service.crear(dto);
    }

    // =========================================================
    // 4. ACTUALIZAR
    // =========================================================
    @PutMapping("/{id}")
    public void actualizar(
            @PathVariable Long id,
            @RequestBody ActivoFijoSaveDTO dto
    ) {
        System.out.println("🔥 Actualizar activo fijo id=" + id);
        service.actualizar(id, dto);
    }

    // =========================================================
    // 5. ELIMINAR
    // =========================================================
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        System.out.println("🔥 Eliminar activo fijo id=" + id);
        service.eliminar(id);
    }
}
