package co.assip.erp.shared.referencias.catalogos.estado_civil;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/estados-civiles")
public class EstadoCivilController {

    private final EstadoCivilService service;

    public EstadoCivilController(EstadoCivilService service) {
        this.service = service;
    }

    @GetMapping
    public List<EstadoCivil> listar() {
        return service.listar();
    }
}
