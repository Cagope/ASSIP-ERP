package co.assip.erp.hojavida.condicionesproteccion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controlador REST — Condiciones de Protección
 * ------------------------------------------------------------
 * Ruta base:
 * /hoja-vida/condiciones-proteccion
 */
@RestController
@RequestMapping("/hoja-vida/condiciones-proteccion")
public class CondicionProteccionController {

    private final CondicionProteccionService service;

    public CondicionProteccionController(
            CondicionProteccionService service
    ) {
        this.service = service;
    }

    /**
     * Lista todos los registros.
     *
     * GET /hoja-vida/condiciones-proteccion
     */
    @GetMapping
    public ResponseEntity<List<CondicionProteccion>> listar() {
        return ResponseEntity.ok(
                service.listar()
        );
    }

    /**
     * Busca un registro por ID.
     *
     * GET /hoja-vida/condiciones-proteccion/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CondicionProteccion> buscarPorId(
            @PathVariable("id") Long idCondicionProteccion
    ) {
        CondicionProteccion condicion = service
                .buscarPorId(idCondicionProteccion)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el registro de condiciones de protección."
                ));

        return ResponseEntity.ok(condicion);
    }

    /**
     * Busca las condiciones de protección de una persona.
     *
     * GET /hoja-vida/condiciones-proteccion/persona/{idDatosPersonal}
     */
    @GetMapping("/persona/{idDatosPersonal}")
    public ResponseEntity<CondicionProteccion> buscarPorPersona(
            @PathVariable Integer idDatosPersonal
    ) {
        CondicionProteccion condicion = service
                .buscarPorPersona(idDatosPersonal)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La persona no tiene registradas condiciones de protección."
                ));

        return ResponseEntity.ok(condicion);
    }

    /**
     * Crea un nuevo registro.
     *
     * POST /hoja-vida/condiciones-proteccion
     */
    @PostMapping
    public ResponseEntity<CondicionProteccion> crear(
            @RequestBody CondicionProteccion condicion
    ) {
        CondicionProteccion creado = service.crear(condicion);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    /**
     * Actualiza un registro existente.
     *
     * PUT /hoja-vida/condiciones-proteccion/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CondicionProteccion> actualizar(
            @PathVariable("id") Long idCondicionProteccion,
            @RequestBody CondicionProteccion condicion
    ) {
        CondicionProteccion actualizado = service.actualizar(
                idCondicionProteccion,
                condicion
        );

        return ResponseEntity.ok(actualizado);
    }

    /**
     * Elimina un registro.
     *
     * DELETE /hoja-vida/condiciones-proteccion/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable("id") Long idCondicionProteccion
    ) {
        service.eliminar(idCondicionProteccion);

        return ResponseEntity.noContent().build();
    }
}