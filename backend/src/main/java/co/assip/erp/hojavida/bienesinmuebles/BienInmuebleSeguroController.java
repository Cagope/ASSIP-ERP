package co.assip.erp.hojavida.bienesinmueblesseguros;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de seguros
 * asociados a bienes inmuebles.
 *
 * Endpoint base:
 * /api/v1/hoja-vida/bienes-inmuebles-seguros
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

    /**
     * Lista todos los seguros asociados a un bien.
     *
     * Devuelve siempre 200 OK, incluso cuando la lista esté vacía.
     */
    @GetMapping("/bien/{idBien}")
    public ResponseEntity<List<BienInmuebleSeguro>> listarPorBien(
            @PathVariable Long idBien
    ) {
        List<BienInmuebleSeguro> lista =
                service.listarPorBien(idBien);

        return ResponseEntity.ok(lista);
    }

    /**
     * Consulta un seguro por su identificador.
     */
    @GetMapping("/{idSeguro}")
    public ResponseEntity<BienInmuebleSeguro> buscarPorId(
            @PathVariable Long idSeguro
    ) {
        return service.buscarPorId(idSeguro)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    /**
     * Crea un nuevo seguro.
     */
    @PostMapping
    public ResponseEntity<BienInmuebleSeguro> crear(
            @RequestBody BienInmuebleSeguro dto
    ) {
        BienInmuebleSeguro guardado =
                service.crear(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(guardado);
    }

    /**
     * Actualiza un seguro existente.
     */
    @PutMapping("/{idSeguro}")
    public ResponseEntity<BienInmuebleSeguro> actualizar(
            @PathVariable Long idSeguro,
            @RequestBody BienInmuebleSeguro dto
    ) {
        return service.actualizar(idSeguro, dto)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    /**
     * Elimina un seguro existente.
     */
    @DeleteMapping("/{idSeguro}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idSeguro
    ) {
        boolean eliminado =
                service.eliminar(idSeguro);

        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}