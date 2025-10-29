package co.assip.erp.shared.referencias.catalogos.parentesco;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/parentescos")
public class ParentescoController {

    private final ParentescoService service;

    public ParentescoController(ParentescoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Parentesco> listar() {
        return service.listar();
    }
}
