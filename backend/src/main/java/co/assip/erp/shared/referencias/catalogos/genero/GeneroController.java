package co.assip.erp.shared.referencias.catalogos.genero;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/generos")
public class GeneroController {

    private final GeneroService service;

    public GeneroController(GeneroService service) {
        this.service = service;
    }

    @GetMapping
    public List<Genero> listar() {
        return service.listar();
    }
}
