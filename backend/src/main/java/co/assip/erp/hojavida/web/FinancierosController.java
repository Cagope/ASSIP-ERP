package co.assip.erp.hojavida.web;

import co.assip.erp.hojavida.service.FinancieroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hoja-vida/datos-personales/{idDatosPersonal}/financiero")
public class FinancierosController {
    private final FinancieroService service;
    public FinancierosController(FinancieroService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<?> getByPersona(@PathVariable Integer idDatosPersonal) {
        return service.getByPersona(idDatosPersonal)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
