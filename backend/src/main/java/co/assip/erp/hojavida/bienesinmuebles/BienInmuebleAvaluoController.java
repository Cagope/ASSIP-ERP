package co.assip.erp.hojavida.bienesinmueblesavaluos;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🧾 Controlador REST — Avalúos de Bienes Inmuebles
 *
 * Endpoint base: /api/v1/hoja-vida/bienes-inmuebles-avaluos
 */
@RestController
@RequestMapping("/hoja-vida/bienes-inmuebles-avaluos")
public class BienInmuebleAvaluoController {

    private final BienInmuebleAvaluoService service;

    public BienInmuebleAvaluoController(
            BienInmuebleAvaluoService service
    ) {
        this.service = service;
    }

    @GetMapping("/bien/{idBien}")
    public ResponseEntity<List<BienInmuebleAvaluo>> listarPorBien(
            @PathVariable Long idBien
    ) {
        List<BienInmuebleAvaluo> lista =
                service.listarPorBien(idBien);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{idAvaluo}")
    public ResponseEntity<BienInmuebleAvaluo> buscarPorId(
            @PathVariable Long idAvaluo
    ) {
        return service.buscarPorId(idAvaluo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BienInmuebleAvaluo> crear(
            @RequestBody BienInmuebleAvaluo dto
    ) {
        BienInmuebleAvaluo guardado =
                service.crear(dto);

        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{idAvaluo}")
    public ResponseEntity<BienInmuebleAvaluo> actualizar(
            @PathVariable Long idAvaluo,
            @RequestBody BienInmuebleAvaluo dto
    ) {
        return service.actualizar(idAvaluo, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{idAvaluo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idAvaluo
    ) {
        if (service.eliminar(idAvaluo)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}