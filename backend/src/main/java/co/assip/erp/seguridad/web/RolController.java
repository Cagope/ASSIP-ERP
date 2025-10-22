package co.assip.erp.seguridad.web;

import co.assip.erp.seguridad.domain.Rol;
import co.assip.erp.seguridad.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seguridad/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    public ResponseEntity<List<Rol>> listar() {
        return ResponseEntity.ok(rolService.listar());
    }

    @GetMapping("/{nombre}")
    public ResponseEntity<Rol> buscar(@PathVariable String nombre) {
        return rolService.buscarPorNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Rol> guardar(@RequestBody Rol rol) {
        return ResponseEntity.ok(rolService.guardar(rol));
    }

    @DeleteMapping("/{idRol}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer idRol) {
        rolService.eliminar(idRol);
        return ResponseEntity.noContent().build();
    }
}
