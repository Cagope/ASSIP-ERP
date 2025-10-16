package co.assip.erp.hojavida.web;

import co.assip.erp.hojavida.dto.ReferenciasPersonalesDTOs.ReferenciaPersonalResponse;
import co.assip.erp.hojavida.service.ReferenciaPersonalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hoja-vida/datos-personales/{idDatosPersonal}/referencia-personal")
public class ReferenciasPersonalesController {

    private final ReferenciaPersonalService service;

    public ReferenciasPersonalesController(ReferenciaPersonalService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getByPersona(@PathVariable Integer idDatosPersonal) {
        return service.getByPersona(idDatosPersonal)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
