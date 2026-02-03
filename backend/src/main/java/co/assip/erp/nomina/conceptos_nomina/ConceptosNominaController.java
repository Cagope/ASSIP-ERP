package co.assip.erp.nomina.conceptos_nomina;

import co.assip.erp.nomina.conceptos_nomina.dto.ConceptoNominaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/conceptos-nomina")
@RequiredArgsConstructor
public class ConceptosNominaController {

    private final ConceptosNominaService service;

    @GetMapping
    public ResponseEntity<List<ConceptoNominaDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<ConceptoNominaDTO> obtener(@PathVariable String codigo) {
        return ResponseEntity.ok(service.obtener(codigo));
    }

    @PostMapping
    public ResponseEntity<Void> crear(
            @RequestBody ConceptoNominaDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Integer idUsuario
    ) {
        Integer usr = (idUsuario != null ? idUsuario : 1);
        service.crear(dto, usr);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<Void> actualizar(
            @PathVariable String codigo,
            @RequestBody ConceptoNominaDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Integer idUsuario
    ) {
        Integer usr = (idUsuario != null ? idUsuario : 1);
        service.actualizar(codigo, dto, usr);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo) {
        service.eliminar(codigo);
        return ResponseEntity.ok().build();
    }
}
