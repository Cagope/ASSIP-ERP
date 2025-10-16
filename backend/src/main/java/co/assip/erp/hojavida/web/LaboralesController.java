package co.assip.erp.hojavida.web;

import co.assip.erp.hojavida.dto.LaboralDTOs.LaboralDetail;
import co.assip.erp.hojavida.service.LaboralesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hoja-vida/datos-personales/{idPersona}/laboral")
public class LaboralesController {

    private final LaboralesService service;

    public LaboralesController(LaboralesService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<LaboralDetail> get(@PathVariable Integer idPersona) {
        return service.tryGetByPersona(idPersona)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build()); // 204 cuando no existe
        // Si prefieres 404, cambia a: .orElse(ResponseEntity.notFound().build());
    }
}
