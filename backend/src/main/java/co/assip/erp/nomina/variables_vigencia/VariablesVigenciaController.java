package co.assip.erp.nomina.variables_vigencia;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.nomina.variables_vigencia.dto.VariablesVigenciaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/variables-vigencia")
@RequiredArgsConstructor
public class VariablesVigenciaController {

    private final VariablesVigenciaService service;
    private final UsuarioSesionService usuarioSesionService;

    @GetMapping
    public ResponseEntity<List<VariablesVigenciaDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VariablesVigenciaDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<Void> crear(
            @RequestBody VariablesVigenciaDTO dto
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.crear(dto, idUsuario);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(
            @PathVariable Integer id,
            @RequestBody VariablesVigenciaDTO dto
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.actualizar(id, dto, idUsuario);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.ok().build();
    }
}
