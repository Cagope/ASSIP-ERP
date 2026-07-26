package co.assip.erp.hojavida.bienesinmuebles;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================
 * Bienes Inmuebles
 * ============================================================
 */
@RestController
@RequestMapping("/hoja-vida/bienes-inmuebles")
public class BienInmuebleController {

    private final BienInmuebleService service;

    public BienInmuebleController(
            BienInmuebleService service
    ) {
        this.service = service;
    }

    // ============================================================
    // CONSULTAS
    // ============================================================

    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<List<BienInmueble>> listarPorPersona(
            @PathVariable Long idDatosPersonal
    ) {

        List<BienInmueble> lista =
                service.listarPorPersona(idDatosPersonal);

        if (lista.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{idBien}")
    public ResponseEntity<BienInmueble> buscarPorIdBien(
            @PathVariable Long idBien
    ) {

        return service.buscarPorIdBien(idBien)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================================================
    // PROCESOS
    // ============================================================

    @PostMapping
    public ResponseEntity<BienInmueble> registrar(
            @RequestBody BienInmueble dto
    ) {

        BienInmueble respuesta =
                service.registrarBienInmueble(dto);

        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{idBien}")
    public ResponseEntity<BienInmueble> actualizar(
            @PathVariable Long idBien,
            @RequestBody BienInmueble dto
    ) {

        return service.actualizarBienInmueble(idBien, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{idBien}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idBien
    ) {

        boolean eliminado =
                service.eliminarBienInmueble(idBien);

        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}