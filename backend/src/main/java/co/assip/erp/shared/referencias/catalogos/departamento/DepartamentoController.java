package co.assip.erp.shared.referencias.catalogos.departamento;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/departamentos")
public class DepartamentoController {

    private final DepartamentoService service;

    public DepartamentoController(DepartamentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Departamento> listar() {
        return service.listar();
    }
}
