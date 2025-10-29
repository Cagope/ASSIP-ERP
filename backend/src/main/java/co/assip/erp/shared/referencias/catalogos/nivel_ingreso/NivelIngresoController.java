package co.assip.erp.shared.referencias.catalogos.nivel_ingreso;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/niveles-ingresos")
public class NivelIngresoController {

    private final NivelIngresoService service;

    public NivelIngresoController(NivelIngresoService service) {
        this.service = service;
    }

    @GetMapping
    public List<NivelIngreso> listar() {
        return service.listar();
    }
}
