package co.assip.erp.hojavida.bienesmaquinaria;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🛡️ Controlador REST — Seguros de Bienes Maquinaria
 *
 * Endpoint base: /api/v1/hoja-vida/bienes-maquinaria-seguros
 */
@RestController
@RequestMapping("/hoja-vida/bienes-maquinaria-seguros")
public class BienMaquinariaSeguroController {

    private final BienMaquinariaSeguroService service;

    public BienMaquinariaSeguroController(
            BienMaquinariaSeguroService service
    ) {
        this.service = service;
    }

    @GetMapping("/bien/{idBien}")
    public ResponseEntity<List<BienMaquinariaSeguro>> listarPorBien(
            @PathVariable Long idBien
    ) {
        List<BienMaquinariaSeguro> lista =
                service.listarPorBien(idBien);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{idSeguro}")
    public ResponseEntity<BienMaquinariaSeguro> buscarPorId(
            @PathVariable Long idSeguro
    ) {
        return service.buscarPorId(idSeguro)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BienMaquinariaSeguro> crear(
            @RequestBody BienMaquinariaSeguro dto
    ) {
        BienMaquinariaSeguro guardado =
                service.crear(dto);

        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{idSeguro}")
    public ResponseEntity<BienMaquinariaSeguro> actualizar(
            @PathVariable Long idSeguro,
            @RequestBody BienMaquinariaSeguro dto
    ) {
        return service.actualizar(idSeguro, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{idSeguro}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idSeguro
    ) {
        if (service.eliminar(idSeguro)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}