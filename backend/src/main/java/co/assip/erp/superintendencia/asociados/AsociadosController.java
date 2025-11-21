package co.assip.erp.superintendencia.asociados;

import co.assip.erp.superintendencia.asociados.dto.AsociadoDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/superintendencia/asociados")
public class AsociadosController {

    private final AsociadosService service;

    public AsociadosController(AsociadosService service) {
        this.service = service;
    }

    @GetMapping
    public List<AsociadoDTO> consultar(@RequestParam String fechaCorte) {
        return service.consultar(fechaCorte);
    }
}
