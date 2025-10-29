package co.assip.erp.shared.referencias.catalogos.tipo_contrato;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/tipos-contratos")
public class TipoContratoController {

    private final TipoContratoService service;

    public TipoContratoController(TipoContratoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoContrato> listar() {
        return service.listar();
    }
}
