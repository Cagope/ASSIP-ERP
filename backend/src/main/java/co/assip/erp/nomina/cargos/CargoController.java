package co.assip.erp.nomina.cargos;

import co.assip.erp.nomina.cargos.dto.CargoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/cargos")
@RequiredArgsConstructor
public class CargoController {

    private final CargoService service;

    // ============================================================
    // ✅ LISTAR
    // ============================================================
    @GetMapping
    public List<CargoDTO> listar() {
        return service.listar();
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    @GetMapping("/{idCargo}")
    public CargoDTO obtener(@PathVariable Integer idCargo) {
        return service.obtener(idCargo);
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody CargoDTO dto) {
        Integer id = service.crear(dto);
        return ResponseEntity.ok(id);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    @PutMapping("/{idCargo}")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer idCargo,
            @RequestBody CargoDTO dto
    ) {
        service.actualizar(idCargo, dto);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // ✅ DESACTIVAR
    // ============================================================
    @DeleteMapping("/{idCargo}")
    public ResponseEntity<?> desactivar(@PathVariable Integer idCargo) {
        service.desactivar(idCargo);
        return ResponseEntity.ok().build();
    }
}
