package co.assip.erp.shared.referencias.catalogos.tipo_bien;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/tipos-bienes")
public class TipoBienController {

    private final TipoBienService service;

    public TipoBienController(TipoBienService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoBien> listar() {
        return service.listar();
    }
}
