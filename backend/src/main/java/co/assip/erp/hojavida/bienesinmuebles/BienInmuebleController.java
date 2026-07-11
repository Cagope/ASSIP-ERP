package co.assip.erp.hojavida.bienesinmuebles;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 🏠 Controlador REST — Bienes Inmuebles del Asociado
 *
 * Endpoint base: /api/v1/hoja-vida/bienes-inmuebles
 */
@RestController
@RequestMapping("/hoja-vida/bienes-inmuebles")
public class BienInmuebleController {

    private final BienInmuebleService service;

    public BienInmuebleController(BienInmuebleService service) {
        this.service = service;
    }

    /** 🔹 Listar bienes inmuebles por asociado */
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

    /** 🔹 Buscar un bien inmueble por ID del bien */
    @GetMapping("/{idBien}")
    public ResponseEntity<BienInmueble> buscarPorIdBien(
            @PathVariable Long idBien
    ) {
        return service.buscarPorIdBien(idBien)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 🔹 Registrar nuevo bien inmueble */
    @PostMapping
    public ResponseEntity<BienInmueble> registrar(
            @RequestBody BienInmueble dto
    ) {
        BienInmueble guardado =
                service.registrarBienInmueble(dto);

        return ResponseEntity.ok(guardado);
    }

    /** 🔹 Actualizar bien inmueble existente */
    @PutMapping("/{idBien}")
    public ResponseEntity<BienInmueble> actualizar(
            @PathVariable Long idBien,
            @RequestBody BienInmueble dto
    ) {
        return service.actualizarBienInmueble(idBien, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 🔹 Eliminar bien inmueble */
    @DeleteMapping("/{idBien}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idBien
    ) {
        if (service.eliminarBienInmueble(idBien)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}