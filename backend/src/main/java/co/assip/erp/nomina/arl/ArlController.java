package co.assip.erp.nomina.arl;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.nomina.arl.dto.ArlDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/arl")
@RequiredArgsConstructor
public class ArlController {

    private final ArlService service;
    private final UsuarioSesionService usuarioSesionService;

    // ============================================================
    // ✅ LISTAR
    // ============================================================
    @GetMapping
    public ResponseEntity<List<ArlDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<ArlDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    @PostMapping
    public ResponseEntity<Void> crear(
            @RequestBody ArlDTO dto
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.crear(dto, idUsuario);

        return ResponseEntity.ok().build();
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(
            @PathVariable Integer id,
            @RequestBody ArlDTO dto
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.actualizar(id, dto, idUsuario);

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
