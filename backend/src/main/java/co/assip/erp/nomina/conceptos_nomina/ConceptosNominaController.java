package co.assip.erp.nomina.conceptos_nomina;

import co.assip.erp.seguridad.service.UsuarioSesionService;
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
    private final UsuarioSesionService usuarioSesionService;

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
            @RequestBody ConceptoNominaDTO dto
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.crear(dto, idUsuario);

        return ResponseEntity.ok().build();
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    @PutMapping("/{codigoConcepto}")
    public ResponseEntity<Void> actualizar(
            @PathVariable String codigoConcepto,
            @RequestBody ConceptoNominaDTO dto
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        service.actualizar(
                codigoConcepto,
                dto,
                idUsuario
        );

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
