package co.assip.erp.shared.referencias.catalogos.ocupacion;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/ocupaciones")
public class OcupacionController {

    private final OcupacionService service;

    public OcupacionController(OcupacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Ocupacion> listar() {
        return service.listar();
    }
}
