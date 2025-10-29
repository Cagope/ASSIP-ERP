package co.assip.erp.shared.referencias.catalogos.tipo_directivo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/tipos-directivos")
public class TipoDirectivoController {

    private final TipoDirectivoService service;

    public TipoDirectivoController(TipoDirectivoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoDirectivo> listar() {
        return service.listar();
    }
}
