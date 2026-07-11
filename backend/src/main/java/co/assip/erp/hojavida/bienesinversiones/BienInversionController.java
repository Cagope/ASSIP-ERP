package co.assip.erp.hojavida.bienesinversiones;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 💰 Controlador REST — Bienes Inversiones del Asociado
 *
 * Endpoint base: /api/v1/hoja-vida/bienes-inversiones
 */
@RestController
@RequestMapping("/hoja-vida/bienes-inversiones")
public class BienInversionController {

    private final BienInversionService service;

    public BienInversionController(
            BienInversionService service
    ) {
        this.service = service;
    }

    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<List<BienInversion>> listarPorPersona(
            @PathVariable Long idDatosPersonal
    ) {
        List<BienInversion> lista =
                service.listarPorPersona(idDatosPersonal);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{idBien}")
    public ResponseEntity<BienInversion> buscarPorIdBien(
            @PathVariable Long idBien
    ) {
        return service.buscarPorIdBien(idBien)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BienInversion> registrar(
            @RequestBody BienInversion dto
    ) {
        BienInversion guardado =
                service.registrarBienInversion(dto);

        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{idBien}")
    public ResponseEntity<BienInversion> actualizar(
            @PathVariable Long idBien,
            @RequestBody BienInversion dto
    ) {
        return service.actualizarBienInversion(idBien, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{idBien}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idBien
    ) {
        if (service.eliminarBienInversion(idBien)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}