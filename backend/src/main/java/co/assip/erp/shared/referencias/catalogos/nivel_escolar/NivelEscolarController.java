package co.assip.erp.shared.referencias.catalogos.nivel_escolar;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/niveles-escolares")
public class NivelEscolarController {

    private final NivelEscolarService service;

    public NivelEscolarController(NivelEscolarService service) {
        this.service = service;
    }

    @GetMapping
    public List<NivelEscolar> listar() {
        return service.listar();
    }
}
