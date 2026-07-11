package co.assip.erp.hojavida.bienesmaquinaria;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ⚙️ Controlador REST — Bienes Maquinaria del Asociado
 *
 * Endpoint base: /api/v1/hoja-vida/bienes-maquinaria
 */
@RestController
@RequestMapping("/hoja-vida/bienes-maquinaria")
public class BienMaquinariaController {

    private final BienMaquinariaService service;

    public BienMaquinariaController(BienMaquinariaService service) {
        this.service = service;
    }

    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<List<BienMaquinaria>> listarPorPersona(
            @PathVariable Long idDatosPersonal
    ) {
        List<BienMaquinaria> lista =
                service.listarPorPersona(idDatosPersonal);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{idBien}")
    public ResponseEntity<BienMaquinaria> buscarPorIdBien(
            @PathVariable Long idBien
    ) {
        return service.buscarPorIdBien(idBien)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BienMaquinaria> registrar(
            @RequestBody BienMaquinaria dto
    ) {
        BienMaquinaria guardado =
                service.registrarBienMaquinaria(dto);

        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{idBien}")
    public ResponseEntity<BienMaquinaria> actualizar(
            @PathVariable Long idBien,
            @RequestBody BienMaquinaria dto
    ) {
        return service.actualizarBienMaquinaria(idBien, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{idBien}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idBien
    ) {
        if (service.eliminarBienMaquinaria(idBien)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}