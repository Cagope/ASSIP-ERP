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

    // ============================================================
    // LISTAR
    // ============================================================
    @GetMapping
    public ResponseEntity<List<ConceptoNominaDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    // ============================================================
    // OBTENER
    // ============================================================
    @GetMapping("/{codigoConcepto}")
    public ResponseEntity<ConceptoNominaDTO> obtener(
            @PathVariable String codigoConcepto
    ) {
        return ResponseEntity.ok(service.obtener(codigoConcepto));
    }

    // ============================================================
    // CREAR
    // ============================================================
    @PostMapping
    public ResponseEntity<Void> crear(
            @RequestBody ConceptoNominaDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Integer idUsuario
    ) {
        Integer usr = (idUsuario != null ? idUsuario : 1);
        service.crear(dto, usr);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    @PutMapping("/{codigoConcepto}")
    public ResponseEntity<Void> actualizar(
            @PathVariable String codigoConcepto,
            @RequestBody ConceptoNominaDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Integer idUsuario
    ) {
        Integer usr = (idUsuario != null ? idUsuario : 1);
        service.actualizar(codigoConcepto, dto, usr);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // ELIMINAR
    // ============================================================
    @DeleteMapping("/{codigoConcepto}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String codigoConcepto
    ) {
        service.eliminar(codigoConcepto);
        return ResponseEntity.ok().build();
    }
}
