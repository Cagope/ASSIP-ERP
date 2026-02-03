package co.assip.erp.nomina.afp;

import co.assip.erp.nomina.afp.dto.AfpDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/afp")
@RequiredArgsConstructor
public class AfpController {

    private final AfpService service;

    // ============================================================
    // ✅ LISTAR
    // ============================================================
    @GetMapping
    public List<AfpDTO> listar() {
        return service.listar();
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    @GetMapping("/{id}")
    public AfpDTO obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    @PostMapping
    public ResponseEntity<Void> crear(
            @RequestBody AfpDTO dto,
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
            @RequestBody AfpDTO dto,
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
