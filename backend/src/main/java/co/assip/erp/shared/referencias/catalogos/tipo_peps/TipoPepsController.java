package co.assip.erp.shared.referencias.catalogos.tipo_peps;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/tipos-peps")
public class TipoPepsController {

    private final TipoPepsService service;

    public TipoPepsController(TipoPepsService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoPeps> listar() {
        return service.listar();
    }
}
