package co.assip.erp.shared.referencias.catalogos.pais;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/paises")
public class PaisController {

    private final PaisService service;

    public PaisController(PaisService service) {
        this.service = service;
    }

    @GetMapping
    public List<Pais> listar() {
        return service.listar();
    }
}
