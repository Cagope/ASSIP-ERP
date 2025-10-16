package co.assip.erp.hojavida.web;

import co.assip.erp.hojavida.dto.DatosFamiliaresDTOs.FamiliarListItem;
import co.assip.erp.hojavida.dto.DatosFamiliaresDTOs.FamiliarDetail;
import co.assip.erp.hojavida.service.DatosFamiliaresService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hoja-vida/datos-personales/{idPersona}/familiares")
public class DatosFamiliaresController {

    private final DatosFamiliaresService service;

    public DatosFamiliaresController(DatosFamiliaresService service) {
        this.service = service;
    }

    @GetMapping
    public Page<FamiliarListItem> list(@PathVariable Integer idPersona,
                                       @RequestParam(value = "q", required = false) String q,
                                       Pageable pageable) {
        return service.list(idPersona, q, pageable);
    }

    @GetMapping("/{idFamiliar}")
    public FamiliarDetail get(@PathVariable Integer idPersona,
                              @PathVariable Integer idFamiliar) {
        return service.get(idPersona, idFamiliar);
    }
}
