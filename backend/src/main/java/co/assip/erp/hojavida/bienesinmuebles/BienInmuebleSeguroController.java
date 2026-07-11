package co.assip.erp.hojavida.bienesinmueblesseguros;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🛡️ Controlador REST — Seguros de Bienes Inmuebles
 *
 * Endpoint base: /api/v1/hoja-vida/bienes-inmuebles-seguros
 */
@RestController
@RequestMapping("/hoja-vida/bienes-inmuebles-seguros")
public class BienInmuebleSeguroController {

    private final BienInmuebleSeguroService service;

    public BienInmuebleSeguroController(
            BienInmuebleSeguroService service
    ) {
        this.service = service;
    }

    @GetMapping("/bien/{idBien}")
    public ResponseEntity<List<BienInmuebleSeguro>> listarPorBien(
            @PathVariable Long idBien
    ) {
        List<BienInmuebleSeguro> lista =
                service.listarPorBien(idBien);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{idSeguro}")
    public ResponseEntity<BienInmuebleSeguro> buscarPorId(
            @PathVariable Long idSeguro
    ) {
        return service.buscarPorId(idSeguro)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BienInmuebleSeguro> crear(
            @RequestBody BienInmuebleSeguro dto
    ) {
        BienInmuebleSeguro guardado =
                service.crear(dto);

        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{idSeguro}")
    public ResponseEntity<BienInmuebleSeguro> actualizar(
            @PathVariable Long idSeguro,
            @RequestBody BienInmuebleSeguro dto
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