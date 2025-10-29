package co.assip.erp.shared.referencias.catalogos.jornada_laboral;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/jornadas-laborales")
public class JornadaLaboralController {

    private final JornadaLaboralService service;

    public JornadaLaboralController(JornadaLaboralService service) {
        this.service = service;
    }

    @GetMapping
    public List<JornadaLaboral> listar() {
        return service.listar();
    }
}
