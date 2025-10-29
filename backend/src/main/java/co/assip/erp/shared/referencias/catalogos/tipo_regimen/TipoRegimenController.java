package co.assip.erp.shared.referencias.catalogos.tipo_regimen;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/tipos-regimen")
public class TipoRegimenController {

    private final TipoRegimenService service;

    public TipoRegimenController(TipoRegimenService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoRegimen> listar() {
        return service.listar();
    }
}
