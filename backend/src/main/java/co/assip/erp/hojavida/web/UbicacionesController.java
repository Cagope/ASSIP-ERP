package co.assip.erp.hojavida.web;

import co.assip.erp.hojavida.dto.UbicacionDTOs;
import co.assip.erp.hojavida.service.UbicacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hoja-vida/datos-personales/{idDatosPersonal}/ubicacion")
public class UbicacionesController {

    private final UbicacionService service;

    public UbicacionesController(UbicacionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getByPersona(@PathVariable Integer idDatosPersonal) {
        return service.getByPersona(idDatosPersonal)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
