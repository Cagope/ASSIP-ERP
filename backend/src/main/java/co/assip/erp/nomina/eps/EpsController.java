package co.assip.erp.nomina.eps;

import co.assip.erp.nomina.eps.dto.EpsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/eps")
@RequiredArgsConstructor
public class EpsController {

    private final EpsService service;

    // ============================================================
    // ✅ LISTAR
    // ============================================================
    @GetMapping
    public List<EpsDTO> listar() {
        return service.listar();
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    @GetMapping("/{id}")
    public EpsDTO obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    @PostMapping
    public Integer crear(@RequestBody EpsDTO dto) {
        return service.crear(dto);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    @PutMapping("/{id}")
    public void actualizar(@PathVariable Integer id, @RequestBody EpsDTO dto) {
        service.actualizar(id, dto);
    }

    // ============================================================
    // ✅ ELIMINAR
    // ============================================================
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}
