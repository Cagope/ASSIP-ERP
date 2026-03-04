package co.assip.erp.nomina.concepto_cuentas_contables;

import co.assip.erp.nomina.concepto_cuentas_contables.dto.ConceptoCuentaContableFormDTO;
import co.assip.erp.nomina.concepto_cuentas_contables.dto.ConceptoCuentaContableListDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nomina/concepto-cuentas-contables")
@RequiredArgsConstructor
public class ConceptoCuentasContablesController {

    private final ConceptoCuentasContablesService service;

    // LISTAR (opcional por agencia)
    @GetMapping
    public ResponseEntity<List<ConceptoCuentaContableListDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    // ============================================================
    // OBTENER (por id)
    // ============================================================
    @GetMapping("/{idMapeo}")
    public ResponseEntity<ConceptoCuentaContableFormDTO> obtener(
            @PathVariable Integer idMapeo
    ) {
        return ResponseEntity.ok(service.obtener(idMapeo));
    }

    // ============================================================
    // CREAR
    // ============================================================
    @PostMapping
    public ResponseEntity<Void> crear(
            @RequestBody ConceptoCuentaContableFormDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Integer idUsuario
    ) {
        Integer usr = (idUsuario != null ? idUsuario : 1);
        service.crear(dto, usr);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    @PutMapping("/{idMapeo}")
    public ResponseEntity<Void> actualizar(
            @PathVariable Integer idMapeo,
            @RequestBody ConceptoCuentaContableFormDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Integer idUsuario
    ) {
        Integer usr = (idUsuario != null ? idUsuario : 1);
        service.actualizar(idMapeo, dto, usr);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // ELIMINAR (SOFT DELETE)
    // ============================================================
    @DeleteMapping("/{idMapeo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Integer idMapeo,
            @RequestHeader(value = "X-User-Id", required = false) Integer idUsuario
    ) {
        Integer usr = (idUsuario != null ? idUsuario : 1);
        service.eliminar(idMapeo, usr);
        return ResponseEntity.ok().build();
    }
}