package co.assip.erp.hojavida.web;

import co.assip.erp.hojavida.service.SarlaftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hoja-vida/datos-personales/{idDatosPersonal}/sarlaft")
public class SarlaftController {

    private final SarlaftService service;

    public SarlaftController(SarlaftService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getByPersona(@PathVariable Integer idDatosPersonal) {
        return service.getByPersona(idDatosPersonal)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
