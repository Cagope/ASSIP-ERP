package co.assip.erp.hojavida.bienesvehiculos;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🛡️ Controlador REST — Seguros de Bienes Vehículos
 *
 * Endpoint base: /api/v1/hoja-vida/bienes-vehiculos-seguros
 */
@RestController
@RequestMapping("/hoja-vida/bienes-vehiculos-seguros")
public class BienVehiculoSeguroController {

    private final BienVehiculoSeguroService service;

    public BienVehiculoSeguroController(
            BienVehiculoSeguroService service
    ) {
        this.service = service;
    }

    @GetMapping("/bien/{idBien}")
    public ResponseEntity<List<BienVehiculoSeguro>> listarPorBien(
            @PathVariable Long idBien
    ) {
        List<BienVehiculoSeguro> lista =
                service.listarPorBien(idBien);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{idSeguro}")
    public ResponseEntity<BienVehiculoSeguro> buscarPorId(
            @PathVariable Long idSeguro
    ) {
        return service.buscarPorId(idSeguro)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BienVehiculoSeguro> crear(
            @RequestBody BienVehiculoSeguro dto
    ) {
        BienVehiculoSeguro guardado =
                service.crear(dto);

        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{idSeguro}")
    public ResponseEntity<BienVehiculoSeguro> actualizar(
            @PathVariable Long idSeguro,
            @RequestBody BienVehiculoSeguro dto
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