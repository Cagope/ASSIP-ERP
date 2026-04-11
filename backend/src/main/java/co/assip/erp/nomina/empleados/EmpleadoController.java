package co.assip.erp.nomina.empleados;

import co.assip.erp.nomina.empleados.dto.EmpleadoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService service;

    // ============================================================
    // ✅ LISTAR
    // ============================================================
    @GetMapping
    public ResponseEntity<List<EmpleadoDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoDTO> obtener(@PathVariable Integer id) {


            return ResponseEntity.ok(service.obtener(id));


    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    @PostMapping
    public ResponseEntity<Integer> crear(
            @RequestBody EmpleadoDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Integer idUsuario
    ) {
        Integer usr = (idUsuario != null ? idUsuario : 1);
        Integer id = service.crear(dto, usr);
        return ResponseEntity.ok(id);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(
            @PathVariable Integer id,
            @RequestBody EmpleadoDTO dto,
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
