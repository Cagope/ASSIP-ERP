package co.assip.erp.shared.referencias.catalogos.tipo_documento;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/tipos-documentos")
public class TipoDocumentoController {

    private final TipoDocumentoService service;

    public TipoDocumentoController(TipoDocumentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoDocumento> listar() {
        return service.listar();
    }
}
