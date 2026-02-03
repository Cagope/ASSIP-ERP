package co.assip.erp.nomina.cesantias;

import co.assip.erp.nomina.cesantias.dto.CesantiasDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/cesantias")
@RequiredArgsConstructor
public class CesantiasController {

    private final CesantiasService service;

    // ============================================================
    // ✅ LISTAR
    // ============================================================
    @GetMapping
    public ResponseEntity<List<CesantiasDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<CesantiasDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    @PostMapping
    public ResponseEntity<Void> crear(
            @RequestBody CesantiasDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Integer idUsuario
    ) {
        Integer usr = (idUsuario != null ? idUsuario : 1);
        service.crear(dto, usr);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(
            @PathVariable Integer id,
            @RequestBody CesantiasDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Integer idUsuario
    ) {
        Integer usr = (idUsuario != null ? idUsuario : 1);
        service.actualizar(id, dto, usr);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // ✅ ELIMINAR
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.ok().build();
    }
}
