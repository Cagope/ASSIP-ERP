package co.assip.erp.shared.referencias.catalogos.tipo_empresa;

import org.springframework.web.bind.annotation.*;
import java.util.List;

//@RestController
//@RequestMapping("/catalogos/tipos-empresas")
public class TipoEmpresaController {

    private final TipoEmpresaService service;

    public TipoEmpresaController(TipoEmpresaService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoEmpresa> listar() {
        return service.listar();
    }
}
