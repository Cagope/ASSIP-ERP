package co.assip.erp.nomina.empleados;

import co.assip.erp.seguridad.service.UsuarioSesionService;
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
    private final UsuarioSesionService usuarioSesionService;

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
            @RequestBody EmpleadoDTO dto
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Integer id =
                service.crear(dto, idUsuario);

        return ResponseEntity.ok(id);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(
            @PathVariable Integer id,
            @RequestBody EmpleadoDTO dto
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
