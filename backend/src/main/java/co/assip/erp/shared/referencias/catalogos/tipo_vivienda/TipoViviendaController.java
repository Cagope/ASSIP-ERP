package co.assip.erp.shared.referencias.catalogos.tipo_vivienda;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/tipos-viviendas")
public class TipoViviendaController {

    private final TipoViviendaService service;

    public TipoViviendaController(TipoViviendaService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoVivienda> listar() {
        return service.listar();
    }
}
